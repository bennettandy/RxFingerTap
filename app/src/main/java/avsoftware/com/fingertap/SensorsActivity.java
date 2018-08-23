package avsoftware.com.fingertap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.pwittchen.reactivesensors.library.ReactiveSensorFilter;
import com.github.pwittchen.reactivesensors.library.ReactiveSensors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SensorsActivity extends AppCompatActivity {

    private CompositeDisposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disposable = new CompositeDisposable();

        disposable.add( new ReactiveSensors(this).observeSensor(Sensor.TYPE_GYROSCOPE)
                .subscribeOn(Schedulers.computation())
                .filter(ReactiveSensorFilter.filterSensorChanged())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reactiveSensorEvent -> {
                    SensorEvent event = reactiveSensorEvent.getSensorEvent();

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    String message = String.format("x = %f, y = %f, z = %f", x, y, z);

                    Timber.d("gyroscope readings %s", message);
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}
