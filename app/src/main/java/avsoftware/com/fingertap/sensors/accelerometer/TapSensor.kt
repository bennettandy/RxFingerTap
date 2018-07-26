package avsoftware.com.fingertap.sensors.accelerometer

import android.view.MotionEvent
import io.reactivex.Observable

class TapSensor(){

    val motionEvents: Observable<MotionEvent> = Observable.empty()

}