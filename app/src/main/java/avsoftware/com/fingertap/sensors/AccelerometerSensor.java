package avsoftware.com.fingertap.sensors;

import avsoftware.com.fingertap.domain.DeviceSensor;
import io.reactivex.Flowable;

public interface AccelerometerSensor extends DeviceSensor {
    Flowable<AccelerometerData> sensorData();
}
