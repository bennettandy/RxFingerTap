package avsoftware.com.fingertap.domain;

import io.reactivex.Completable;

public class TapsRecorder {

    public Completable startRecording() {
        return Completable.complete();
    }

    public void rightTap(){}
    public void leftTap(){}
}
