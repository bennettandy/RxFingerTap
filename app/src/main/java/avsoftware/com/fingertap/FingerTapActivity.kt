package avsoftware.com.fingertap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import avsoftware.com.fingertap.databinding.ActivityFingerTapBinding
import avsoftware.com.fingertap.recorder.*
import avsoftware.com.fingertap.sensors.Accelerometer
import avsoftware.com.fingertap.sensors.TapData
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class FingerTapActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    private val viewModel = TapViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityFingerTapBinding.inflate(LayoutInflater.from(this))

        binding.viewModel = viewModel
        binding.activity = this

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()


        // Build Tap Event Recorder


        val accelerometer = Accelerometer(this)


        // Tap Event File Envelope - provides Header and Footer
        val tapDataEnvelope = FileEnvelope("<ORKTaskResult: 0x1c4293d30; identifier: \"twoFingerTappingIntervalTask\"; results: (\n" +
                "    <ORKStepResult: 0x1c0277d40; identifier: \"instruction\"; enabledAssistiveTechnology: None; results: ()>,\n" +
                "    <ORKStepResult: 0x1c4666d80; identifier: \"instruction1.left\"; enabledAssistiveTechnology: None; results: ()>,\n" +
                "    <ORKStepResult: 0x1c467a680; identifier: \"tapping.left\"; enabledAssistiveTechnology: None; results: (\n" +
                "        <ORKFileResult: 0x1c046fe80; identifier: \"accelerometer\"; fileURL: file:///private/var/mobile/Containers/Data/Application/769E8198-5573-4F2E-8048-4A02A84D461E/Documents/recorder-F3B0675A-53B7-4910-8B3F-3ACE19E58511/accel_F3B0675A_53B7_4910_8B3F_3ACE19E58511-20180724164954 (115432 bytes)>,\n" +
                "        <ORKTappingIntervalResult: 0x1c41119d0; identifier: \"tapping.left\"; samples: (",
                ")>,\n" +
                        "    <ORKStepResult: 0x1c4666f80; identifier: \"conclusion\"; enabledAssistiveTechnology: None; results: ()>\n" +
                        ")>\n")



//        val tapData: Flowable<SensorTap.TapData> = Flowable.range(100, 100)
//                .map { SensorTap.TapData(1, it.toFloat(), it*2.toFloat(), 100f, 200f) }
//                .doOnNext { Timber.d("Tap Data: $it") }

        // Tap Events Pipeline
        val tapEventPipeline: Flowable<TapData> = viewModel.tapDataPipeline
                .take(20, TimeUnit.SECONDS)
                .doOnNext{ Timber.d("Tap Event $it") }

        // Tap Event Recorder
        val recorderTaps = FileRecorderImpl()

        // Consume and record Tap Events
        val tapsRecordWriter = FileRecordWriter(externalCacheDir, "taps.txt")
        disposable.add(
                recorderTaps.writeToFile(tapEventPipeline,
                        tapsRecordWriter, tapDataEnvelope )
                        .doOnSuccess { Timber.d("DONE TAPS") }
                        .doOnError { Timber.e(it, "Failed to write file") }
                        .doOnSuccess { Timber.d("DONE $it") }
                        .subscribe())


        // Accelerometer File Envelope, provides file header and footer
        val accelerometerEnvelope = FileEnvelope("{ \"items\": [ ", " ] }")

        // Accelerometer Recorder
        val recorderAccelerometer = FileRecorderImpl()

        val accelRecordWriter = FileRecordWriter(externalCacheDir, "accelerometer.txt")

        // Consume and record Accelerometer events
        disposable.add(
                recorderAccelerometer.writeToFile(accelerometer.accelerometerFlowable, // INFINITE stream
                        accelRecordWriter, accelerometerEnvelope)
                        .doOnSubscribe { Timber.d("START") }
                        .doOnSuccess { Timber.d("DONE") }
                        .doOnDispose { Timber.d("Disposed") }
                        .doOnError { Timber.e(it, "Failed to write file") }
                        .subscribe { _: RecordedFile?, _: Throwable? -> {} })

        // Stop the accelerometer recorder after time delay
        disposable.add(
                Observable.just(true).delay(10, TimeUnit.SECONDS)
                        .doOnNext { recorderAccelerometer.stopRecording() }
                        .doOnNext { Timber.d("Stop Triggered") }
                        .subscribe()
        )

    }

    fun startTest() {
        Timber.d("Start Test Clicked")
        disposable.add(viewModel.startTest
                .doOnSubscribe { Timber.d("Subscribed") }
                .doOnComplete { Timber.d("Completed") }
                .subscribe())
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
}
