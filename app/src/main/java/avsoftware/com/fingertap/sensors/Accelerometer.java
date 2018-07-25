package avsoftware.com.fingertap.sensors;

import io.reactivex.Flowable;

public interface Accelerometer {

    Flowable<AccelerometerData> sensorData();
}
