package avsoftware.com.fingertap.sensors;

import android.content.Context;
import android.hardware.Sensor;

import com.github.pwittchen.reactivesensors.library.ReactiveSensorEvent;
import com.github.pwittchen.reactivesensors.library.ReactiveSensorFilter;
import com.github.pwittchen.reactivesensors.library.ReactiveSensors;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class AccelerometerImpl implements Accelerometer {

    private Flowable<AccelerometerData> mAccelerometerFlowable;

    public AccelerometerImpl(Context context, int sampleFrequencyUs) {
        mAccelerometerFlowable = new ReactiveSensors(context)
                .observeSensor(Sensor.TYPE_ACCELEROMETER, sampleFrequencyUs)
                .subscribeOn(Schedulers.computation())
                .filter(ReactiveSensorFilter.filterSensorChanged())
                .map(ReactiveSensorEvent::getSensorEvent)
                .map(sensorEvent -> new AccelerometerData(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], sensorEvent.timestamp))
                .share()
        ;
    }

    @Override
    public Flowable<AccelerometerData> sensorData() {
        return mAccelerometerFlowable;
    }
}
