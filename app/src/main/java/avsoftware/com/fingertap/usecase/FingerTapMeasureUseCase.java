package avsoftware.com.fingertap.usecase;

import android.content.Context;
import android.databinding.ObservableInt;
import android.hardware.SensorManager;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import avsoftware.com.fingertap.recorder.FileEnvelope;
import avsoftware.com.fingertap.recorder.FileRecordWriter;
import avsoftware.com.fingertap.recorder.FileRecorder;
import avsoftware.com.fingertap.recorder.FileRecorderImpl;
import avsoftware.com.fingertap.recorder.RecordWriter;
import avsoftware.com.fingertap.recorder.Recordable;
import avsoftware.com.fingertap.recorder.RecordedFile;
import avsoftware.com.fingertap.sensors.Accelerometer;
import avsoftware.com.fingertap.sensors.AccelerometerData;
import avsoftware.com.fingertap.sensors.TapData;
import avsoftware.com.fingertap.sensors.TapSensor;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

public class FingerTapMeasureUseCase {

    private final static int LEFT_TAP_ID = 100;
    private final static int RIGHT_TAP_ID = 200;

    private final static int COUNT_DOWN_SECONDS = 10;

    private final TapSensor mTapSensor;
    private final Accelerometer mAccelerometer;

    /**
     * Tap Data File Envelope
     */
    private static final FileEnvelope tapDataEnvelope = new FileEnvelope("<ORKTaskResult: 0x1c4293d30; identifier: \"twoFingerTappingIntervalTask\"; results: (\n" +
                    "    <ORKStepResult: 0x1c0277d40; identifier: \"instruction\"; enabledAssistiveTechnology: None; results: ()>,\n" +
                    "    <ORKStepResult: 0x1c4666d80; identifier: \"instruction1.left\"; enabledAssistiveTechnology: None; results: ()>,\n" +
                    "    <ORKStepResult: 0x1c467a680; identifier: \"tapping.left\"; enabledAssistiveTechnology: None; results: (\n" +
                    "        <ORKFileResult: 0x1c046fe80; identifier: \"accelerometer\"; fileURL: file:///private/var/mobile/Containers/Data/Application/769E8198-5573-4F2E-8048-4A02A84D461E/Documents/recorder-F3B0675A-53B7-4910-8B3F-3ACE19E58511/accel_F3B0675A_53B7_4910_8B3F_3ACE19E58511-20180724164954 (115432 bytes)>,\n" +
                    "        <ORKTappingIntervalResult: 0x1c41119d0; identifier: \"tapping.left\"; samples: (",
            ")>,\n" +
                    "    <ORKStepResult: 0x1c4666f80; identifier: \"conclusion\"; enabledAssistiveTechnology: None; results: ()>\n" +
                    ")>\n");

    /**
     * Accelerometer Data File Envelope
     */
    private static final FileEnvelope accelerometerDataEnvelope = new FileEnvelope("{ items: [", "] }");

    private static final String TAP_DATA_SEPARATOR = ", ";
    private static final String ACCELEROMETER_DATA_SEPARATOR = ", ";

    public FingerTapMeasureUseCase(@NotNull Context context, @NotNull Observable<MotionEvent> leftTouchEvents, @NotNull Observable<MotionEvent> rightTouchEvents){
        mTapSensor = new TapSensor(leftTouchEvents, rightTouchEvents);
        mAccelerometer = new Accelerometer(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public Flowable<RecordedFile> setUpProcessingPipeline(@NotNull File tapsOutputFile, @NotNull File accelerometerOutputFile, @NotNull ObservableInt countDownObservable, ObservableInt totalTaps){
        // Tap Events emitted from this pipeline
        Flowable<TapData> tapEvents = mTapSensor.tapEventPipeline(LEFT_TAP_ID, RIGHT_TAP_ID)
                // increment tap counter for each tap event
                .doOnNext(tapData -> totalTaps.set(totalTaps.get()+1))
                .share();

        // Stop Flag will stop file writers consuming further events and close their respective files
        Completable countDownTimer = createCountDownTimer(countDownObservable);

        // Timer Starts by taking the first tap event and emits true when the timer completes
        Observable<Boolean> stopFlag = tapEvents.take(1) // first tap should start the timer
                .map( __ -> true)
                .flatMapSingle( aBoolean -> countDownTimer
                        .toSingleDefault(true)) // emit true on completion, this is the 'stop' event
                .toObservable()
                .startWith(false);

        // Accelerometer events
        Flowable<AccelerometerData> accelerometerEvents = mAccelerometer.getAccelerometerFlowable().throttleFirst(250, TimeUnit.MILLISECONDS);

        // Create Recorder Pipelines for Tap Events and Accelerometer Events
        // Both infinite streams, recording stops when stopFlag flips to true
        Single<RecordedFile> recordedTapsFile = createFileRecorderPipeline(tapEvents, tapsOutputFile, TAP_DATA_SEPARATOR, tapDataEnvelope, stopFlag);
        Single<RecordedFile> recordedAccelerometerFile = createFileRecorderPipeline(accelerometerEvents, accelerometerOutputFile, ACCELEROMETER_DATA_SEPARATOR, accelerometerDataEnvelope, stopFlag);

        // Merge both singles into Flowable, emits recorded file info when recording completes
        return Single.merge(recordedTapsFile, recordedAccelerometerFile);
    }

    private Single<RecordedFile> createFileRecorderPipeline(Flowable<? extends Recordable> dataEvents, File outputFile, String dataSeparator, FileEnvelope fileEnvelope, Observable<Boolean> stopFlag){
        FileRecorder recorder = new FileRecorderImpl();

        // customise file writer for data type
        RecordWriter dataWriter = new FileRecordWriter(outputFile);

        // construct file recorder pipeline
        return recorder.writeToFile(dataEvents, dataWriter, fileEnvelope, dataSeparator, stopFlag );
    }

    /**
     * Count down length seconds, updating countDownStatus and then Complete
     */
    private Completable createCountDownTimer( ObservableInt countDownStatus ){
        return Observable.zip(Observable.range(0, COUNT_DOWN_SECONDS),
                Observable.interval(1, TimeUnit.SECONDS), (integer, aLong) -> COUNT_DOWN_SECONDS - integer)
                .doOnSubscribe(__ -> countDownStatus.set(COUNT_DOWN_SECONDS))
                .map( integer -> integer - 1)
                .startWith(COUNT_DOWN_SECONDS)
                .doOnNext(countDownStatus::set) // update UI counter
                .ignoreElements();
    }
}
