package avsoftware.com.fingertap;

import android.app.Application;

import timber.log.Timber;

public class FingerTapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setUpLogging();
    }

    private void setUpLogging() {
        Timber.plant(new Timber.DebugTree());
    }
}
