package avsoftware.com.fingertap.sensors

import android.os.SystemClock
import android.view.MotionEvent
import avsoftware.com.fingertap.recorder.Recordable
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class TapSensor (val tapOneEvents: Observable<MotionEvent>, val tapTwoEvents: Observable<MotionEvent>) {

    fun tapEventPipeline( leftButtonId: Int, rightButtonId: Int): Flowable<TapData> {
        return Observable.merge(
                createMotionToTapEventPipeline(leftButtonId, tapOneEvents),
                createMotionToTapEventPipeline(rightButtonId, tapTwoEvents)
        ).toFlowable(BackpressureStrategy.BUFFER)
    }

    private fun createMotionToTapEventPipeline( buttonId: Int, motionEvents: Observable<MotionEvent> ): Observable<TapData> {

        // Fixme: simplify this, removing Subject
        val currentTapTime: BehaviorSubject<Long> = BehaviorSubject.createDefault(0L)
        //val startTime: BehaviorSubject<Long> = BehaviorSubject.createDefault(0L)

        //val startTime = Observable.just(System.currentTimeMillis()).cacheWithInitialCapacity(1);

        // Down Event Time is Stored in currentTapTime
        val downEventTime: Completable = motionEvents
                .doOnNext { Timber.d("MotionEvent $it") }
                .filter { it.action == MotionEvent.ACTION_DOWN }
                .map { System.currentTimeMillis() }
                .doOnNext { Timber.d("Down $it")}
                .doOnNext { currentTapTime.onNext(it) }
                .ignoreElements()

        // Up Event is mapped to TapData instance - downTime property taken from currentTapTime
        val upEvents = motionEvents.filter { it.action == MotionEvent.ACTION_UP }
                .doOnNext { Timber.d("UP $it")}
                .withLatestFrom(currentTapTime, BiFunction { motion: MotionEvent, downTime: Long -> Pair(motion, downTime)  })
                // construct TapData event from pair of Up event and Down Time
                .map { TapData(buttonId, it.second.toFloat(), (SystemClock.currentThreadTimeMillis() - it.second).toFloat(), it.first.rawX, it.first.rawY) }


        return Observable.merge(downEventTime.toObservable(), upEvents)
    }
}

class

// CLASS to store tap data
data class TapData(var iButton: Int, var fStartTime: Float, var fDuration: Float, var x: Float, var y: Float) : Recordable {
    override fun getRecordableString(): String { return "\"<ORKTappingSample: 0x00000000; button: $iButton; timestamp: $fStartTime; timestamp: $fDuration; location: {$x, $y}>\"" }
}