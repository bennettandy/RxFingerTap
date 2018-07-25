package avsoftware.com.fingertap;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;

import avsoftware.com.fingertap.domain.TapsRecorder;
import io.reactivex.Completable;
import timber.log.Timber;

public class TapViewModel {

    public final ObservableBoolean isRightHand;

    public final ObservableInt totalTaps;

    private final TapsRecorder recorder;

    public TapViewModel(TapsRecorder tapsRecorder){
        isRightHand = new ObservableBoolean(true);
        totalTaps = new ObservableInt(0);
        recorder = tapsRecorder;
    }

    public Completable startRecording(){
        return recorder.startRecording();
    }

    public void leftButton(){
        recorder.leftTap();
    }

    public void rightButton(){
        recorder.rightTap();
    }

    public void uploadData(){
        Timber.d("Upload Data");
    }
}
