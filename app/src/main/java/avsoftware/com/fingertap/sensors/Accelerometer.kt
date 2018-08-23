package avsoftware.com.fingertap.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import avsoftware.com.fingertap.recorder.Recordable

import com.github.pwittchen.reactivesensors.library.ReactiveSensorFilter
import com.github.pwittchen.reactivesensors.library.ReactiveSensors

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class Accelerometer(context: Context, sampleFrequencyUs: Int = SensorManager.SENSOR_DELAY_NORMAL) {

    val accelerometerFlowable: Flowable<AccelerometerData> = ReactiveSensors(context)
            .observeSensor(Sensor.TYPE_GRAVITY, sampleFrequencyUs)
            .subscribeOn(Schedulers.computation())
            .filter(ReactiveSensorFilter.filterSensorChanged())
            .map {
                with(it.sensorEvent) {
                    AccelerometerData(values[0], values[1], values[2], timestamp)
                }
            }
            .share()
}

data class AccelerometerData(val x: Float, val y: Float, val z: Float, val timestamp: Long) : Recordable {
    override fun getRecordableString() = "{\"y\":${y.toBigDecimal()}, \"timestamp\":$timestamp, \"z\":${z.toBigDecimal()}, \"x\":${x.toBigDecimal()} }"
}