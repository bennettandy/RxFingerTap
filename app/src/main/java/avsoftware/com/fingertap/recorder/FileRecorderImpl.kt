package avsoftware.com.fingertap.recorder

import avsoftware.com.fingertap.sensors.Recordable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.*

class FileRecorderImpl : FileRecorder {

    override fun writeToFile(recordable: Flowable<out Recordable>, outputDirectory: File, filename: String): Single<RecordedFile> {

        return Single.just(FileStream(File(outputDirectory, filename)))
                .observeOn(Schedulers.io()) // requires IO scheduler to write to filesystem
                .doOnSuccess { fileStream -> fileStream.writeData("{items:[".toByteArray()) }
                .flatMap { filestream ->
                    recordable
                            .doOnNext { event -> Timber.d("Event: %s", event.toString()) }
                            .doOnNext { event -> filestream.writeData(event.getBytes()) }
                            .ignoreElements()
                            .toSingleDefault(filestream)
                }
                .doOnSuccess { fileStream: FileStream -> fileStream.writeData("]}".toByteArray()) }
                .doOnSuccess { it.close() }
                .map { fileStream -> fileStream.file }
                .map { RecordedFile(it)}
    }

    internal inner class FileStream(val file: File) {

        val bufferedOutputStream: BufferedOutputStream by lazy { BufferedOutputStream(file.outputStream()) }

        fun writeData(data: ByteArray) {
            bufferedOutputStream.write(data)
        }

        fun close() {
            bufferedOutputStream.flush()
            bufferedOutputStream.close()
        }
    }
}
