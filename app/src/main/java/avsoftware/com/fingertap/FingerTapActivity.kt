package avsoftware.com.fingertap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import avsoftware.com.fingertap.databinding.ActivityFingerTapBinding
import avsoftware.com.fingertap.recorder.FileEnvelope
import avsoftware.com.fingertap.recorder.FileRecorderImpl
import avsoftware.com.fingertap.recorder.RecordedFile
import avsoftware.com.fingertap.sensors.accelerometer.Accelerometer
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

class FingerTapActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    private val viewModel = TapViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityFingerTapBinding.inflate(LayoutInflater.from(this))


        binding.viewModel = viewModel
        setContentView(binding.root)

        disposable.add(
                viewModel.combinedTaps()
                        .doOnNext{ Timber.d("TAP: $it")}
                        .subscribe()
        )
    }

    override fun onStart() {
        super.onStart()

        val accelerometer = Accelerometer(this)
        val fileRecorder = FileRecorderImpl()
        val fileEnvelope = FileEnvelope("{ \"items\": [ ", " ] }")

        disposable.add(
                fileRecorder.writeToFile(accelerometer
                .accelerometerFlowable, // infinite stream //.take(10, TimeUnit.SECONDS),
                cacheDir, "OutPutFile1.txt", fileEnvelope)
                        .doOnSubscribe { Timber.d("START") }
                        .doOnSuccess { Timber.d("DONE")}
                        .doOnDispose { Timber.d("Disposed") }
                        .doOnError { Timber.e(it, "Failed to write file") }
                        .subscribe { _: RecordedFile?, _: Throwable? -> {} })

        disposable.add(
                Observable.just(true).delay(10, TimeUnit.SECONDS)
                        .doOnNext{ flag -> fileRecorder.stopFlag.accept(flag) }
                        .doOnNext{ Timber.d("Stop Triggered")}
                        .subscribe()
        )


    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
}
