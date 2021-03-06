package me.hyuck9.calculator

import android.app.Application
import android.widget.Toast
import com.jakewharton.threetenabp.AndroidThreeTen
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

	companion object {
		var toast: Toast? = null
	}

	override fun onCreate() {
		super.onCreate()

		AndroidThreeTen.init(this)

		if (BuildConfig.DEBUG) {
			Logger.addLogAdapter(AndroidLogAdapter())
			Timber.plant(Timber.DebugTree())
			Timber.d("Timber is initialized.")
		}
	}
}