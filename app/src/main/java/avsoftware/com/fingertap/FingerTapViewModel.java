package avsoftware.com.fingertap;

import android.content.Context;
import android.databinding.ObservableInt;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import avsoftware.com.fingertap.recorder.RecordedFile;
import avsoftware.com.fingertap.usecase.FingerTapMeasureUseCase;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public class FingerTapViewModel {

    private final FingerTapMeasureUseCase fingerTapUseCase;

    public final ObservableInt countdownProgress;

    public final ObservableInt totalTaps;

    public FingerTapViewModel(@NotNull  Context context, Observable<MotionEvent> leftTouchEvents, @NotNull Observable<MotionEvent> rightTouchEvents){
        fingerTapUseCase = new FingerTapMeasureUseCase(context, leftTouchEvents, rightTouchEvents );
        countdownProgress = new ObservableInt(0);
        totalTaps = new ObservableInt(99);
    }

    public Observable<RecordedFile> getProcessorPipeline(Context context){
        File outputDirectory = context.getExternalCacheDir();
        File tapsOutputFile = new File( outputDirectory, "tap_events.txt");
        File accelerometerOutputFile = new File( outputDirectory, "accelerometer_events.txt");

        return getEventProcessorPipeline(tapsOutputFile, accelerometerOutputFile).toObservable();
    }

    private Flowable<RecordedFile> getEventProcessorPipeline(@NotNull File tapsOutputFile, @NotNull File accelerometerOutputFile){
        return fingerTapUseCase.setUpProcessingPipeline(tapsOutputFile, accelerometerOutputFile, countdownProgress);
    }
}
