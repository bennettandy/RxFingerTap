package avsoftware.com.fingertap

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import avsoftware.com.fingertap.databinding.ActivityFingerTapBinding
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class FingerTapActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    private lateinit var viewModel: FingerTapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityFingerTapBinding.inflate(LayoutInflater.from(this))

        // left events
        val leftTouches: Observable<MotionEvent> = RxView.touches(binding.tapSensorOne).subscribeOn(AndroidSchedulers.mainThread())
        val rightTouches: Observable<MotionEvent> = RxView.touches(binding.tapSensorTwo).subscribeOn(AndroidSchedulers.mainThread())

        viewModel = FingerTapViewModel(this, leftTouches, rightTouches )

        binding.viewModel = viewModel
        binding.activity = this

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        // Build Tap Event Recorder

        val recordPipeline = viewModel.getProcessorPipeline(this)

        disposable.add(
                recordPipeline
                        .doOnSubscribe { Timber.d("Subscribed to processor pipeline") }
                        .doOnNext{ Timber.d("Recorded: $it")}
                        .doOnComplete{ Timber.d("Completed")}
                        .doOnError { Timber.e(it, "Failed") }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete{ Toast.makeText(baseContext, "DONE", Toast.LENGTH_LONG).show() }
                        .subscribe()
        )
    }


    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
}