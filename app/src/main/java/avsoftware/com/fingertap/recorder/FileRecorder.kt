package avsoftware.com.fingertap.recorder

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.io.File

interface FileRecorder {
    fun writeToFile(recordable: Flowable<out Recordable>, recordWriter: RecordWriter, envelope: FileEnvelope, stopFlag: Observable<Boolean>): Single<RecordedFile>
}

// Implemented by Recordable Data Events to enable them to format their contents
interface Recordable {
    fun getRecordableString(): String
}

// Result class returns details of the recorded File
data class RecordedFile(val file: File)

// File Envelope defines optional file header and footer
class FileEnvelope(val header: String = "", val footer: String = "")

class FileRecorderImpl : FileRecorder {

    override fun writeToFile(recordable: Flowable<out Recordable>, recordWriter: RecordWriter, envelope: FileEnvelope, stopFlag: Observable<Boolean>): Single<RecordedFile> {

        return Single.just(recordWriter)
                .observeOn(Schedulers.io()) // requires IO scheduler to write to filesystem

                .doOnSuccess { it.write(envelope.header) } // write file header from envelope

                .flatMap { filestream ->
                    recordable.take(1) // write the first element without a leading separator
                            .doOnNext { filestream.write(it.getRecordableString()) }
                            .ignoreElements().toSingleDefault(filestream)
                }

                .flatMap { filestream ->
                    recordable.skip(1) // write all subsequent elements with a leading separator
                            .withLatestFrom(stopFlag.toFlowable(BackpressureStrategy.BUFFER),
                                    BiFunction { t1:Recordable, t2:Boolean -> Pair(t1, t2)  })
                            .takeUntil { it.second } // stop taking events once this flag is triggered
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
    override fun writeSeparator() = write(separator)
    override fun close() = bufferedWriter.close()
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
