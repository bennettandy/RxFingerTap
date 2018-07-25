package avsoftware.com.fingertap.sensors;

import io.reactivex.Flowable;

public class AccelerometerSensorImpl implements AccelerometerSensor  {

    private Accelerometer mAccelerometer;

    public AccelerometerSensorImpl(Accelerometer accelerometer) {
        mAccelerometer = accelerometer;
    }

    @Override
    public Flowable<AccelerometerData> sensorData() {
        return mAccelerometer.sensorData();
    }
}
