package avsoftware.com.fingertap.recorder

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.io.File

interface FileRecorder {
    fun writeToFile(recordable: Flowable<out Recordable>,
                    recordWriter: RecordWriter,
                    envelope: FileEnvelope,
                    separator: String = ", ",
                    stopFlag: Observable<Boolean> ): Single<RecordedFile>
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

    override fun writeToFile(recordable: Flowable<out Recordable>,
                             recordWriter: RecordWriter,
                             envelope: FileEnvelope,
                             separator: String,
                             stopFlag: Observable<Boolean> ): Single<RecordedFile> {

        // Events flow until stopFlag emits True
        val stoppableEventStream = Flowable
                .combineLatest(recordable, stopFlag
                        .toFlowable(BackpressureStrategy.BUFFER), BiFunction { t1: Recordable, t2: Boolean -> Pair(t1, t2) })
                .takeUntil{ it.second }
                .map { it.first }

        // emits separators, initially an empty separator
        val separatorStream = Flowable.just(separator)
                .startWith("")

        return Single.just(recordWriter)

                // requires IO scheduler to write to filesystem
                .observeOn(Schedulers.io())

                // write file header from envelope
                .doOnSubscribe { recordWriter.write(envelope.header) }

                .flatMap { filestream ->
                    stoppableEventStream
                            .withLatestFrom(separatorStream, BiFunction { recordable: Recordable, sep: String -> Pair(recordable, sep)  })
                            .doOnNext { filestream.write(it.second) } // write data separator
                            .doOnNext { filestream.write(it.first.getRecordableString()) } // write data item
                            .ignoreElements()
                            .toSingleDefault(filestream)
                }

                .doOnSuccess { it.write(envelope.footer) } // write file footer from envelope
                .doFinally { recordWriter.close() } // Make sure we always close the file

                // emit Recorded File instance
                .map { it.getFileObject() }
                .map { RecordedFile(it) }
    }
}

/**
 * Groups the file instance with buffered writer
 * More readable than using Pair<File,BufferedWriter>
 */
class FileRecordWriter(val file: File) : RecordWriter {

    private val bufferedWriter by lazy { file.bufferedWriter() }
    override fun getFileObject() = this.file
    override fun write(data: String) = bufferedWriter.write(data)
    override fun close() = bufferedWriter.close()
}

/**
 * Mockable interface
 */
interface RecordWriter {
    fun write(data: String)
    fun getFileObject(): File
    fun close()
}
