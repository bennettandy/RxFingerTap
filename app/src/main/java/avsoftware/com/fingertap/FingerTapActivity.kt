package avsoftware.com.fingertap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import avsoftware.com.fingertap.databinding.ActivityFingerTapBinding
import avsoftware.com.fingertap.recorder.RecordedFile
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FingerTapActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    private lateinit var viewModel: FingerTapViewModel

    private lateinit var recorderPipeline: Observable<RecordedFile>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityFingerTapBinding.inflate(LayoutInflater.from(this))

        // left touch events
        val leftTouches: Observable<MotionEvent> = RxView.touches(binding.tapSensorOne)
                .subscribeOn(AndroidSchedulers.mainThread())
        // right touch events
        val rightTouches: Observable<MotionEvent> = RxView.touches(binding.tapSensorTwo)
                .subscribeOn(AndroidSchedulers.mainThread())

        viewModel = FingerTapViewModel(this, leftTouches, rightTouches )

        binding.viewModel = viewModel

        setContentView(binding.root)

        // Build Tap Event Recorder
        recorderPipeline = viewModel.getProcessorPipeline(this)
    }

    override fun onResume() {
        super.onResume()

        disposable.add(
                recorderPipeline
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete{ Toast.makeText(baseContext, "DONE", Toast.LENGTH_LONG).show() }
                        .subscribeOn( Schedulers.computation())
                        .subscribe()
        )
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }
}