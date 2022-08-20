package me.hyuck9.calculator.view.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import me.hyuck9.calculator.App
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.ActivityMainBinding
import me.hyuck9.calculator.view.base.BaseActivity
import me.hyuck9.calculator.view.viewmodel.CalculatorInputViewModel
import me.hyuck9.calculator.view.viewmodel.HistoryViewModel
import splitties.resources.str

const val ADMOB_AD_UNIT_ID = "ca-app-pub-3581307060625507/6810642041"
//const val DEV_ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
var currentNativeAd: NativeAd? = null

@AndroidEntryPoint
class MainActivity : BaseActivity() {

	private val binding: ActivityMainBinding by binding(R.layout.activity_main)
	private val calculatorFragment: CalculatorFragment by lazy {
		supportFragmentManager.findFragmentById(R.id.fcv_container)!!
			.childFragmentManager
			.fragments[0] as CalculatorFragment
	}
	private val historyViewModel by viewModels<HistoryViewModel>()
	private val calcViewModel by viewModels<CalculatorInputViewModel>()

	private var backKeyPressedTime: Long = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding.apply {
			title = "Calculator"
			bindViews()
		}

		MobileAds.initialize(this)
	}

	private fun ActivityMainBinding.bindViews() {
		setSupportActionBar(toolbar)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.history_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.delete_history -> {
				showDeleteHistoryDialog()
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onBackPressed() {

		if (calculatorFragment.isHistoryPanelOpened()) {
			calculatorFragment.closeHistoryPanel()
		} else {
			if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
				backKeyPressedTime = System.currentTimeMillis()
				calcViewModel.showToastMessage(R.string.message_back_button_press)
			} else {
				App.toast?.cancel()
				// TODO: 광고 띄우거나....
				finish()
			}
		}

	}

	override fun onDestroy() {
		currentNativeAd?.destroy()
		super.onDestroy()
	}


//	private fun populateNativeAdView(nativeAd: NativeAd, unifiedAdBinding: AdUnifiedBinding) {
//		val nativeAdView = unifiedAdBinding.root as NativeAdView
//
//		nativeAdView.iconView = unifiedAdBinding.adAppIcon
//		nativeAdView.headlineView = unifiedAdBinding.adHeadline
//		nativeAdView.bodyView = unifiedAdBinding.adBody
//		nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
//
//		unifiedAdBinding.adHeadline.text = nativeAd.headline
//		unifiedAdBinding.adBody.text = nativeAd.body
//		unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
//		unifiedAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
//
//		nativeAdView.setNativeAd(nativeAd)
//	}
//
//	private fun refreshAd() {
//		val builder = AdLoader.Builder(this, ADMOB_AD_UNIT_ID)
//
//		builder.forNativeAd { nativeAd ->
//			if (isDestroyed || isFinishing || isChangingConfigurations) {
//				nativeAd.destroy()
//				return@forNativeAd
//			}
//
//			currentNativeAd?.destroy()
//			currentNativeAd = nativeAd
//			val unifiedAdBinding = AdUnifiedBinding.inflate(layoutInflater)
//			populateNativeAdView(nativeAd, unifiedAdBinding)
//			binding.adFrame.apply {
//				removeAllViews()
//				addView(unifiedAdBinding.root)
//			}
//		}
//
//		val adLoader = builder.withAdListener(object : AdListener() {
//			override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//				Logger.i("Failed to load native ad with error - domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}")
//			}
//		}).build()
//
//		adLoader.loadAd(AdRequest.Builder().build())
//	}


	private fun showDeleteHistoryDialog() {
		MaterialAlertDialogBuilder(this)
			.setTitle(str(R.string.dialog_title_delete))
			.setMessage(str(R.string.dialog_message_delete))
			.setIcon(R.drawable.ic_delete_history)
			.setPositiveButton(android.R.string.ok) { _, _ ->
				historyViewModel.deleteAllHistory()
				if (calculatorFragment.isHistoryPanelOpened()) {
					calculatorFragment.closeHistoryPanel()
				}
			}
			.setNegativeButton(android.R.string.cancel) { dialog, _ ->
				dialog.dismiss()
			}
			.create()
			.show()
	}
}