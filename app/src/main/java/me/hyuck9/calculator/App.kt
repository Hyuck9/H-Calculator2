package me.hyuck9.calculator

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import me.hyuck9.calculator.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

	override fun onCreate() {
		super.onCreate()

		startKoin {
			androidContext(this@App)
			modules(viewModelModule)
		}

		if (BuildConfig.DEBUG) {
			Logger.addLogAdapter(AndroidLogAdapter())
			Timber.plant(Timber.DebugTree())
			Timber.d("Timber is initialized.")
		}
	}
}