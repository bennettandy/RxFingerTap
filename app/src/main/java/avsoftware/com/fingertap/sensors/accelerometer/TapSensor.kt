package avsoftware.com.fingertap.sensors.accelerometer

import android.os.SystemClock
import android.view.MotionEvent
import avsoftware.com.fingertap.sensors.Recordable
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject

class TapSensor(){

    val tapOneRelay: Relay<MotionEvent> = PublishRelay.create()
    val tapTwoRelay: Relay<MotionEvent> = PublishRelay.create()

    fun tapEventPipeline( leftButtonId: Int, rightButtonId: Int): Flowable<TapData> {
        return Observable.merge(
                createMotionToTapEventPipeline(leftButtonId, tapOneRelay),
                createMotionToTapEventPipeline(rightButtonId, tapTwoRelay)
        ).toFlowable(BackpressureStrategy.BUFFER)
    }

    private fun createMotionToTapEventPipeline( buttonId: Int, motionEvents: Observable<MotionEvent>  ): Observable<TapData> {

        val currentTapTime: BehaviorSubject<Long> = BehaviorSubject.createDefault(0L)

        // Down Event Time is Storex in currentTapTime
        val downEvents = motionEvents.filter { t: MotionEvent -> t.action == MotionEvent.ACTION_DOWN }
                .doOnNext { t -> currentTapTime.onNext(SystemClock.currentThreadTimeMillis()) }
                .ignoreElements()

        // Up Event is mapped to TapData instance - downTime property taken from currentTapTime
        val upEvents = motionEvents.filter { t: MotionEvent -> t.action == MotionEvent.ACTION_UP }
                .withLatestFrom(currentTapTime, BiFunction { motion: MotionEvent, downTime: Long -> Pair(motion, downTime)  })
                .map { pair: Pair<MotionEvent, Long> -> TapData(buttonId, pair.second.toFloat(), (SystemClock.currentThreadTimeMillis() - pair.second).toFloat(), pair.first.rawX, pair.first.rawY) }


        return Observable.merge(downEvents.toObservable(), upEvents)
    }

    // CLASS to store tap data
    data class TapData(var iButton: Int, var fStartTime: Float, var fDuration: Float, var x: Float, var y: Float) : Recordable {
        override fun getRecordableString(): String { return "\"<ORKTappingSample: 0x00000000; button: $iButton; timestamp: $fStartTime; timestamp: $fDuration; location: {$x, $y}>\"" }
    }

//    fun mapMotionEventToTapData( event: MotionEvent): SensorTap.TapData {
//
//        var index = event.actionIndex
//        val action = event.actionMasked
//        val pointerId = event.getPointerId(index)
//
//        if (SensorTap.iTimer == 0L) { SensorTap.iTimer = System.currentTimeMillis() }                               // start timer
//
//        when (action) {
//            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> if (iFingerCount == 0) {
//                touchTime = timer
//                fingerId = pointerId
//                iFingerCount++
//            }
//
//            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> if (iFingerCount > 0 && fingerId == pointerId) {
//                iFingerCount--
//                behObs.onNext(SensorTap.TapData(iButton, touchTime, timer - touchTime, event.rawX, event.rawY))
//            }
//        }
//    }
}