package avsoftware.com.fingertap

import android.annotation.SuppressLint
import android.databinding.BindingAdapter
import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.view.MotionEvent
import android.widget.ImageView
import avsoftware.com.fingertap.recorder.TapRecorder
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import java.io.File

class TapViewModel {

    val isRightHand: ObservableBoolean = ObservableBoolean(true)

    val totalTaps: ObservableInt = ObservableInt(0)

    val tapRecorder: TapRecorder by lazy {
        val directory: File = FingerTapApplication.instance?.cacheDir ?: File("")
        TapRecorder("tapsOutput.txt", directory)
    }

    val tapOneRelay: Relay<MotionEvent> = tapRecorder.tapOneRelay
    val tapTwoRelay: Relay<MotionEvent> = tapRecorder.tapTwoRelay

    val visibility: ObservableBoolean = ObservableBoolean(true)


    fun combinedTaps(): Observable<MotionEvent> {
        return Observable.merge( tapOneRelay, tapTwoRelay )
    }


}

@SuppressLint("ClickableViewAccessibility")
@BindingAdapter("motionEventsX")
fun bindMotionEvents(imageView: ImageView, eventRelay: Relay<MotionEvent>) {
    imageView.setOnTouchListener({ v, event -> eventRelay.accept(event); true })
}

