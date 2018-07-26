package avsoftware.com.fingertap.recorder

import java.io.File

import avsoftware.com.fingertap.sensors.Recordable
import io.reactivex.Flowable
import io.reactivex.Single

interface FileRecorder {
    fun writeToFile(recordable: Flowable<out Recordable>, outputDirectory: File, filename: String): Single<RecordedFile>
}
