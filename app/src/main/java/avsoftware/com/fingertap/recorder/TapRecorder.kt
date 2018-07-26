package avsoftware.com.fingertap.recorder

import android.view.MotionEvent
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import timber.log.Timber
import java.io.File

class TapRecorder(val outputFileName: String, val fileLocation: File ) {

    val tapOneRelay: Relay<MotionEvent> = PublishRelay.create()
    val tapTwoRelay: Relay<MotionEvent> = PublishRelay.create()


}