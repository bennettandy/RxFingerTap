package avsoftware.com.fingertap.sensors.location

import android.content.Context
import avsoftware.com.fingertap.sensors.DeviceSensor
import io.reactivex.Flowable
import ru.solodovnikov.rx2locationmanager.LocationRequestBuilder
import ru.solodovnikov.rx2locationmanager.RxLocationManager

interface LocationSensor : DeviceSensor<LocationData>

class LocationSensorImpl(ctx: Context) : LocationSensor {

    private val rxLocationManager: RxLocationManager by lazy { RxLocationManager(ctx) }
    private val locationRequestBuilder: LocationRequestBuilder by lazy { LocationRequestBuilder(ctx) }


    override fun sensorData(): Flowable<LocationData> {


        return Flowable.just(LocationData())
    }
}