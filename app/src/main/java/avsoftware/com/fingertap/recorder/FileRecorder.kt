package avsoftware.com.fingertap.recorder

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.io.File

interface FileRecorder {
    fun writeToFile(recordable: Flowable<out Recordable>, recordWriter: RecordWriter, envelope: FileEnvelope = FileEnvelope()): Single<RecordedFile>
}

interface Recordable {
    fun getRecordableString(): String
}

// Result class returns details of the recorded File
data class RecordedFile(val file: File)

// File Envelope defines optional file header and footer
class FileEnvelope(val header: String = "", val footer: String = "")

class FileRecorderImpl : FileRecorder {

    private var stopFlag: Relay<Boolean> = BehaviorRelay.createDefault(false)

    fun stopRecording() = stopFlag.accept(true)

    override fun writeToFile(recordable: Flowable<out Recordable>, recordWriter: RecordWriter, envelope: FileEnvelope): Single<RecordedFile> {

        return Single.just(recordWriter)
                .observeOn(Schedulers.io()) // requires IO scheduler to write to filesystem

                .doOnSuccess { fileStream ->
                    fileStream.write(envelope.header) } // write file header from envelope

                .flatMap { filestream ->
                    recordable.take(1) // write the first element without a leading separator
                            .doOnNext { recordable -> filestream.write(recordable.getRecordableString()) }
                            .ignoreElements().toSingleDefault(filestream)
                }

                .flatMap { filestream ->
                    recordable.skip(1) // write all subsequent elements with a leading separator
                            .withLatestFrom(this.stopFlag.toFlowable(BackpressureStrategy.BUFFER), BiFunction { dataEvent: Recordable, shouldStop: Boolean -> Pair(dataEvent, shouldStop)  })
                            .takeUntil { pair -> pair.second } // stop taking events once this flag is triggered
                            .map { it.first} // no longer care about stop flag, so map back to the data Event object
                            .doOnNext { filestream.writeSeparator() } // write data separator
                            .doOnNext { filestream.write(it.getRecordableString()) } // write data item
                            .ignoreElements()
                            .toSingleDefault(filestream) // output just the original FileRecordWriter Object so we can complete the file
                }

                .doOnSuccess { it.write(envelope.footer) } // write file footer from envelope
                .doOnSuccess { it.close() }
                .map { it.getFileObject() }
                .map { RecordedFile(it) }
    }
}

/**
 * Groups the file instance with buffered writer
 * More readable than using Pair<File,BufferedWriter>
 */
class FileRecordWriter(val file: File, val separator: String = ", ") : RecordWriter {

    private val bufferedWriter by lazy { file.bufferedWriter() }

    override fun getFileObject() = this.file

    override fun write(data: String) = bufferedWriter.write(data)

    override fun writeSeparator() {
        write(separator)
    }

    override fun close() {
        bufferedWriter.flush()
        bufferedWriter.close()
    }
}

/**
 * Mockable interface
 */
interface RecordWriter {
    fun write(data: String)
    fun writeSeparator()
    fun getFileObject(): File
    fun close()
}
