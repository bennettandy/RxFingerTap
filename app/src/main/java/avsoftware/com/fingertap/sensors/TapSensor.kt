package avsoftware.com.fingertap.sensors

import android.os.SystemClock
import android.view.MotionEvent
import avsoftware.com.fingertap.recorder.Recordable
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject

class TapSensor {

    val tapOneRelay: Relay<MotionEvent> = PublishRelay.create()
    val tapTwoRelay: Relay<MotionEvent> = PublishRelay.create()

    fun tapEventPipeline( leftButtonId: Int, rightButtonId: Int): Flowable<TapData> {
        return Observable.merge(
                createMotionToTapEventPipeline(leftButtonId, tapOneRelay),
                createMotionToTapEventPipeline(rightButtonId, tapTwoRelay)
        ).toFlowable(BackpressureStrategy.BUFFER)
    }

    private fun createMotionToTapEventPipeline( buttonId: Int, motionEvents: Observable<MotionEvent> ): Observable<TapData> {

        val currentTapTime: BehaviorSubject<Long> = BehaviorSubject.createDefault(0L)
        val startTime: BehaviorSubject<Long> = BehaviorSubject.createDefault(0L)

        // Down Event Time is Stored in currentTapTime
        val downEvents = motionEvents.filter { it.action == MotionEvent.ACTION_DOWN }
                .doOnSubscribe{ startTime.onNext(System.currentTimeMillis()) }
                .doOnNext { currentTapTime.onNext(System.currentTimeMillis()) }
                .ignoreElements()

        // Up Event is mapped to TapData instance - downTime property taken from currentTapTime
        val upEvents = motionEvents.filter { it.action == MotionEvent.ACTION_UP }
                .withLatestFrom(currentTapTime, BiFunction { motion: MotionEvent, downTime: Long -> Pair(motion, downTime)  })
                // construct TapData event from pair of Up event and Down Time
                .map { TapData(buttonId, it.second.toFloat(), (SystemClock.currentThreadTimeMillis() - it.second).toFloat(), it.first.rawX, it.first.rawY) }


        return Observable.merge(downEvents.toObservable(), upEvents)
    }
}

class

// CLASS to store tap data
data class TapData(var iButton: Int, var fStartTime: Float, var fDuration: Float, var x: Float, var y: Float) : Recordable {
    override fun getRecordableString(): String { return "\"<ORKTappingSample: 0x00000000; button: $iButton; timestamp: $fStartTime; timestamp: $fDuration; location: {$x, $y}>\"" }
}