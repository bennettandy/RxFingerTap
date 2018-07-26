package avsoftware.com.fingertap

import android.app.Application
import android.content.Context

import timber.log.Timber

class FingerTapApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        setUpLogging()
    }

    private fun setUpLogging() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        var instance: FingerTapApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}
