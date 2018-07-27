package avsoftware.com.fingertap.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import avsoftware.com.fingertap.recorder.Recordable

import com.github.pwittchen.reactivesensors.library.ReactiveSensorFilter
import com.github.pwittchen.reactivesensors.library.ReactiveSensors

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class Accelerometer(context: Context, sampleFrequencyUs: Int = SensorManager.SENSOR_DELAY_NORMAL ) {

    val accelerometerFlowable: Flowable<AccelerometerData> = ReactiveSensors(context)
                .observeSensor(Sensor.TYPE_GRAVITY, sampleFrequencyUs)
                .subscribeOn(Schedulers.computation())
                .filter(ReactiveSensorFilter.filterSensorChanged())
                .map { it.sensorEvent }
                .map { sensorEvent -> AccelerometerData(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2], sensorEvent.timestamp) }
                .share()
}

data class  AccelerometerData( val x: Float, val y: Float, val z: Float, val timestamp: Long ) : Recordable {
    override fun getRecordableString() = "{\"y\":${y.toBigDecimal()}, \"timestamp\":$timestamp, \"z\":${z.toBigDecimal()}, \"x\":${x.toBigDecimal()} }"
}