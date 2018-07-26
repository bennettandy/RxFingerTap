package avsoftware.com.fingertap.sensors

import android.view.MotionEvent
import android.view.View
import io.reactivex.subjects.PublishSubject


/**
 * Created by Ubaid on 25/07/2018.
 */
// Class attached to a view(button), gives tap updates based on when user taps the view and when lifts the finger
// A tap event with (TapData object) is sent, when user lifts the finger
// The timer starts from first tap

class SensorTap(private val view: View, private val iButton: Int) {
    private var iFingerCount = 0
    private var fingerId: Int = 0
    private var touchTime: Float = 0.toFloat()
    private var behObs : PublishSubject<TapData>

    init {
        setupTapListener()
        behObs = PublishSubject.create()
    }

    // Method returns time from 1st tap
    private val timer: Float
        get() = (System.currentTimeMillis() - iTimer) / 1000f

    // CLASS to store tap data
    data class TapData(var iButton: Int, var fStartTime: Float, var fDuration: Float, var x: Float, var y: Float) : Recordable {
        override fun getRecordableString(): String { return "\"<ORKTappingSample: 0x00000000; button: $iButton; timestamp: $fStartTime; timestamp: $fDuration; location: {$x, $y}>\"" }
    }

    // METHODS for overriding sensor interface
    fun onData() = behObs
    fun unSubscribe() { view.setOnTouchListener(null) }

    // METHOD to resets the timer, call it for new reading
    fun reset() {
        iTimer = 0
        fingerId = 0
    }

    // METHOD sets up touch events listener
    private fun setupTapListener() {
        view.setOnTouchListener { view, event ->
            var index = event.actionIndex
            val action = event.actionMasked
            val pointerId = event.getPointerId(index)

            if (iTimer == 0L) { iTimer = System.currentTimeMillis() }                               // start timer

            when (action) {
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> if (iFingerCount == 0) {
                    touchTime = timer
                    fingerId = pointerId
                    iFingerCount++
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> if (iFingerCount > 0 && fingerId == pointerId) {
                    iFingerCount--
                    behObs.onNext(TapData(iButton, touchTime, timer - touchTime, event.rawX, event.rawY))
                }
            }

            true
        }

    }

    // used for timer, as time will be counted from start of first tap
    companion object {
        private var iTimer: Long = 0
    }
}