package avsoftware.com.fingertap

import android.annotation.SuppressLint
import android.databinding.BindingAdapter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.view.MotionEvent
import android.widget.ImageView
import avsoftware.com.fingertap.sensors.TapData
import avsoftware.com.fingertap.sensors.TapSensor
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Flowable

class TapViewModel {

    val testIsRunning: ObservableBoolean = ObservableBoolean(false)
    
    val LEFT_BUTTON_ID = 100;
    val RIGHT_BUTTON_ID = 200;

    val tapSensor = TapSensor()

    val tapOneRelay: Relay<MotionEvent> = tapSensor.tapOneRelay
    val tapTwoRelay: Relay<MotionEvent> = tapSensor.tapTwoRelay

    val tapDataPipeline: Flowable<TapData> = tapSensor.tapEventPipeline(LEFT_BUTTON_ID, RIGHT_BUTTON_ID)

    val isRightHand: ObservableBoolean = ObservableBoolean(true)

    val totalTaps: ObservableInt = ObservableInt(0)
}

@SuppressLint("ClickableViewAccessibility")
@BindingAdapter("motionEventsX")
fun bindMotionEvents(imageView: ImageView, eventRelay: Relay<MotionEvent>) {
    imageView.setOnTouchListener({ v, event -> eventRelay.accept(event); true })
}

