package avsoftware.com.fingertap.sensors

import android.os.SystemClock
import android.view.MotionEvent
import avsoftware.com.fingertap.recorder.Recordable
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class TapSensor (val tapOneEvents: Observable<MotionEvent>, val tapTwoEvents: Observable<MotionEvent>) {

    fun tapEventPipeline( leftButtonId: Int, rightButtonId: Int): Flowable<TapData> {
        return Observable.merge(
                // Events from Button One
                createMotionToTapEventPipeline(leftButtonId, tapOneEvents),
                // Events from Button Two
                createMotionToTapEventPipeline(rightButtonId, tapTwoEvents)
        ).toFlowable(BackpressureStrategy.BUFFER)
    }

    private fun createMotionToTapEventPipeline( buttonId: Int, motionEvents: Observable<MotionEvent> ): Observable<TapData> {

        val sharedMotionEvent = motionEvents.share();

        // Last DOWN Event Time is emitted
        val downEventTime: Observable<Long> = sharedMotionEvent
                .filter { it.action == MotionEvent.ACTION_DOWN }
                .map { System.currentTimeMillis() }

        // UP Event is mapped to TapData instance - downTime taken from last Down Event
        return sharedMotionEvent.filter { it.action == MotionEvent.ACTION_UP }
                .withLatestFrom(downEventTime, BiFunction { motion: MotionEvent, downTime: Long -> Pair(motion, downTime)  })
                .map{
                    with(it){
                        TapData(buttonId, second.toFloat(), (SystemClock.currentThreadTimeMillis() - second).toFloat(), first.rawX, first.rawY)
                    }
                }
    }
}

// CLASS to store tap data
data class TapData(var iButton: Int, var fStartTime: Float, var fDuration: Float, var x: Float, var y: Float) : Recordable {
    override fun getRecordableString(): String { return "\"<ORKTappingSample: 0x00000000; button: $iButton; timestamp: $fStartTime; timestamp: $fDuration; location: {$x, $y}>\"" }
}