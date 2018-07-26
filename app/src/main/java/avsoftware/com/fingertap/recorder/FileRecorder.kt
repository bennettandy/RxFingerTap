package avsoftware.com.fingertap.recorder

import avsoftware.com.fingertap.sensors.Recordable
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.io.File

interface FileRecorder {
    fun writeToFile(recordable: Flowable<out Recordable>, outputDirectory: File, filename: String = "testfile", envelope: FileEnvelope = FileEnvelope()): Single<RecordedFile>
}

// Result class returns details of the recorded File
data class RecordedFile(val file: File)

// File Envelope defines optional file header and footer
class FileEnvelope(val header: String = "", val footer: String = "")

class FileRecorderImpl : FileRecorder {

    var stopFlag: Relay<Boolean> = BehaviorRelay.createDefault(false)

    override fun writeToFile(recordable: Flowable<out Recordable>, outputDirectory: File, filename: String, envelope: FileEnvelope): Single<RecordedFile> {

        return Single.just(FileStream(File(outputDirectory, filename)))
                .observeOn(Schedulers.io()) // requires IO scheduler to write to filesystem

                .doOnSuccess { fileStream -> fileStream.write(envelope.header) } // write file header from envelope

                .flatMap { filestream ->
                    recordable.take(1) // write the first element without a leading separator
                            .doOnNext { recordable -> filestream.write(recordable.getRecordableString()) }
                            .ignoreElements().toSingleDefault(filestream)
                }

                .flatMap { filestream ->
                    recordable.skip(1) // write all subsequent elements with a leading separator
                            .withLatestFrom(this.stopFlag.toFlowable(BackpressureStrategy.BUFFER), BiFunction { dataEvent: Recordable, shouldStop: Boolean -> Pair(dataEvent, shouldStop)  })
                            .takeUntil { pair -> pair.second } // stop taking events once this flag is triggered
                            .map { pair -> pair.first} // no longer care about stop flag, so map back to the data Event object
                            .doOnNext { filestream.writeSeparator() } // write data separator
                            .doOnNext { filestream.write(it.getRecordableString()) } // write data item
                            .ignoreElements()
                            .toSingleDefault(filestream) // output just the original FileStream Object so we can complete the file
                }

                .doOnSuccess { fileStream: FileStream -> fileStream.write(envelope.footer) } // write file footer from envelope
                .doOnSuccess { it.close() }
                .map { fileStream -> fileStream.file }
                .map { RecordedFile(it) }
    }

    internal inner class FileStream(val file: File, val separator: String = ", ") {

        val bufferedWriter by lazy { file.bufferedWriter() }

        fun write(data: String) = bufferedWriter.write(data)

        fun writeSeparator() {
            write(separator)

        }

        fun close() {
            bufferedWriter.flush()
            bufferedWriter.close()
        }
    }
}
