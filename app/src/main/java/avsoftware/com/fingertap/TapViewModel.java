package avsoftware.com.fingertap;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;

import timber.log.Timber;

public class TapViewModel {

    public final ObservableBoolean isRightHand;

    public final ObservableInt totalTaps;


    public TapViewModel( ){
        isRightHand = new ObservableBoolean(true);
        totalTaps = new ObservableInt(0);
    }


    public void leftButton(){
    }

    public void rightButton(){

    }

    public void uploadData(){
        Timber.d("Upload Data");
    }
}
