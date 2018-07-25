package avsoftware.com.fingertap.domain;

import io.reactivex.Flowable;

public interface DeviceSensor<T> {
    Flowable<T> sensorData();
}
