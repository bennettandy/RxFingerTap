package avsoftware.com.fingertap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import avsoftware.com.fingertap.databinding.ActivityFingerTapBinding
import avsoftware.com.fingertap.recorder.FileRecorderImpl
import avsoftware.com.fingertap.sensors.accelerometer.Accelerometer
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class FingerTapActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFingerTapBinding.inflate(LayoutInflater.from(this))
        binding.viewModel = TapViewModel()
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        val accelerometer = Accelerometer(this)
        val fileRecorder = FileRecorderImpl()

        disposable.add(
                fileRecorder.writeToFile(accelerometer
                .accelerometerFlowable.take(10, TimeUnit.SECONDS),
                externalCacheDir, "OutPutFile1.txt")
                        .doOnSuccess { Timber.d("DONE")}
                        .subscribe())

    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
}
