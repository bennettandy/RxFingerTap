package avsoftware.com.fingertap.sensors

import io.reactivex.Flowable

interface DeviceSensor<T> {
    fun sensorData(): Flowable<T>
}
