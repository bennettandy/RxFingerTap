package avsoftware.com.fingertap

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.BindingAdapter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.databinding.ObservableLong
import android.view.MotionEvent
import android.widget.ImageView
import avsoftware.com.fingertap.recorder.FileEnvelope
import avsoftware.com.fingertap.recorder.FileRecorderImpl
import avsoftware.com.fingertap.recorder.RecordedFile
import avsoftware.com.fingertap.sensors.Accelerometer
import avsoftware.com.fingertap.sensors.TapData
import avsoftware.com.fingertap.sensors.TapSensor
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

class TapViewModel {

    companion object {
        const val LEFT_BUTTON_ID = 100;
        const val RIGHT_BUTTON_ID = 200;

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

        // Accelerometer File Envelope, provides file header and footer
        val accelerometerEnvelope = FileEnvelope("{ \"items\": [ ", " ] }")

    }

    val testIsRunning: ObservableBoolean = ObservableBoolean(false)

    private val tapSensor = TapSensor()

    val tapOneRelay: Relay<MotionEvent> = tapSensor.tapOneRelay
    val tapTwoRelay: Relay<MotionEvent> = tapSensor.tapTwoRelay

    val tapDataPipeline: Flowable<TapData> = tapSensor.tapEventPipeline(LEFT_BUTTON_ID, RIGHT_BUTTON_ID)
            .doOnNext { totalTaps.set(totalTaps.get() + 1) }

    val isRightHand: ObservableBoolean = ObservableBoolean(true)

    val totalTaps: ObservableInt = ObservableInt(0)

    val testProgress: ObservableLong = ObservableLong(0L)

    // Timer to drive progress bar and
    val startTest: Completable = Observable.intervalRange(0L, 100L, 0L, 100, TimeUnit.MILLISECONDS)
            .doOnSubscribe { testIsRunning.set(true) }
            .doOnNext { testProgress.set(it) }
            .doOnComplete { testIsRunning.set(false) }
            .ignoreElements()

    // Create Accelerometer Recorder
    fun createAccelerometerRecorder(ctx: Context, recordDirectory: File = ctx.externalCacheDir, fileName: String = "accelerometer.txt" ): Single<RecordedFile> {

        val accelerometer = Accelerometer(ctx)

        // Accelerometer Recorder
        val recorderAccelerometer = FileRecorderImpl()

        return recorderAccelerometer.writeToFile(accelerometer.accelerometerFlowable, // INFINITE stream
                recordDirectory, fileName, accelerometerEnvelope)
                .doOnSubscribe { Timber.d("START") }
                .doOnSuccess { Timber.d("DONE") }
                .doOnDispose { Timber.d("Disposed") }
                .doOnError { Timber.e(it, "Failed to write file") }
    }

    // Create Tap Event Recorder Pipeline
    fun createTapEventRecorder(ctx: Context, recordDirectory: File = ctx.externalCacheDir, fileName: String = "taps.txt" ): Single<RecordedFile> {

        // Tap Event Recorder
        val recorderTaps = FileRecorderImpl()

        // Consume and record Tap Events
        return recorderTaps.writeToFile(tapDataPipeline,
                        recordDirectory, fileName, tapDataEnvelope )
                        .doOnSuccess { Timber.d("DONE TAPS") }
                        .doOnError { Timber.e(it, "Failed to write file") }
                        .doOnSuccess { Timber.d("DONE $it") }
    }

    fun createCombinedEventRecorder(ctx: Context, recordDirectory: File = ctx.externalCacheDir, tapsFileName: String = "taps.txt", accelerometerFileName: String = "accelerometer.txt"){

        val tapsRecorder = createTapEventRecorder(ctx, recordDirectory, tapsFileName)
        val accelRecorder = createAccelerometerRecorder(ctx, recordDirectory, accelerometerFileName)

    }
}

@SuppressLint("ClickableViewAccessibility")
@BindingAdapter("motionEventsX")
fun bindMotionEvents(imageView: ImageView, eventRelay: Relay<MotionEvent>) {
    imageView.setOnTouchListener({ v, event -> eventRelay.accept(event); true })
}

