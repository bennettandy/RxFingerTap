package avsoftware.com.fingertap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import avsoftware.com.fingertap.databinding.ActivityFingerTapBinding
import avsoftware.com.fingertap.domain.TapsRecorder
import avsoftware.com.fingertap.recorder.FileRecorderImpl
import avsoftware.com.fingertap.recorder.RecordedFile
import avsoftware.com.fingertap.sensors.AccelerometerImpl
import avsoftware.com.fingertap.sensors.AccelerometerSensor
import avsoftware.com.fingertap.sensors.AccelerometerSensorImpl
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class FingerTapActivity : AppCompatActivity() {

    lateinit var sensor: AccelerometerSensor

    val fileRecorder = FileRecorderImpl()

    val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFingerTapBinding.inflate(LayoutInflater.from(this))
        binding.viewModel = TapViewModel(TapsRecorder())
        setContentView(binding.root)





        // create instance of sensor
        sensor = AccelerometerSensorImpl(AccelerometerImpl(this, 20000))

    }

    override fun onStart() {
        super.onStart()

        val sensorEventStream = sensor.sensorData().take(5, TimeUnit.SECONDS)
        
        val writeSensorEventsToFile1 : Single<RecordedFile> = fileRecorder.writeToFile(sensorEventStream, externalCacheDir, "OutPutFile1.txt")
        val writeSensorEventsToFile2 : Single<RecordedFile> = fileRecorder.writeToFile(sensorEventStream, externalCacheDir, "OutPutFile2.txt")
        val writeSensorEventsToFile3 : Single<RecordedFile> = fileRecorder.writeToFile(sensorEventStream, externalCacheDir, "OutPutFile3.txt")

        val merged : Flowable<RecordedFile> = Single.merge(writeSensorEventsToFile1, writeSensorEventsToFile2, writeSensorEventsToFile3)

        val completable : Completable = merged.ignoreElements()

        disposable.add( merged
                .doOnNext { Timber.d("Saved File $it")  }
                .doOnComplete { Timber.d("ALL COMPLETE") }
                .subscribe())

    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
}
