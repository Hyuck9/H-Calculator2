package me.hyuck9.calculator

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import timber.log.Timber

class App : Application() {

	override fun onCreate() {
		super.onCreate()

		if (BuildConfig.DEBUG) {
			Logger.addLogAdapter(AndroidLogAdapter())
			Timber.plant(Timber.DebugTree())
			Timber.d("Timber is initialized.")
		}
	}
}