package me.hyuck9.calculator.view.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator.ofFloat
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.AdUnifiedBinding
import me.hyuck9.calculator.databinding.FragmentCalculatorBinding
import me.hyuck9.calculator.extensions.animateWeight
import me.hyuck9.calculator.extensions.observeLiveData
import me.hyuck9.calculator.extensions.updateWeight
import me.hyuck9.calculator.extensions.weight
import me.hyuck9.calculator.view.base.BaseFragment
import me.hyuck9.calculator.view.custom.CalculatorEditText
import me.hyuck9.calculator.view.custom.DraggablePanel
import me.hyuck9.calculator.view.viewmodel.CalculatorInputViewModel
import me.hyuck9.calculator.view.viewmodel.HistoryViewModel

@AndroidEntryPoint
class CalculatorFragment : BaseFragment() {

	private val calcViewModel by activityViewModels<CalculatorInputViewModel>()
	private val historyViewModel by activityViewModels<HistoryViewModel>()

	private val historyFragment = HistoryFragment()

	private lateinit var binding: FragmentCalculatorBinding

	private val mainActivity by lazy { activity as MainActivity }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		binding = binding<FragmentCalculatorBinding>(
			inflater,
			R.layout.fragment_calculator,
			container
		).apply {
			viewModel = calcViewModel
			bindViews()
			bindListeners()
			addObservers()
			setupHistoryPanel()
			addMenuProvider()
		}

		return binding.root
	}

	private fun FragmentCalculatorBinding.bindViews() {
		inputField.input.setOnTextSizeChangeListener(formulaOnTextSizeChangeListener)
		childFragmentManager.beginTransaction()
			.replace(R.id.historyContainer, historyFragment)
			.commit()
	}

	private fun FragmentCalculatorBinding.bindListeners() {
		layoutButtonCalc.btnEqual.setOnClickListener {
			calcViewModel.equalClicked { history ->
				historyViewModel.insertHistory(history)
			}
		}
	}

	private fun FragmentCalculatorBinding.addObservers() {
		observeLiveData(calcViewModel.inputLiveData) {
			calcViewModel.calculateOutput()
		}
		observeLiveData(calcViewModel.expressionLiveData) {
			calcViewModel.setViewExpression()
			calcViewModel.setInputState()
		}
		observeLiveData(historyViewModel.selectedHistory) {
			calcViewModel.setExpression(it)
			if (draggablePanel.isOpen()) {
				draggablePanel.smoothPanelClose(300)
			}
		}
	}

	private fun FragmentCalculatorBinding.setupHistoryPanel() {
		draggablePanel.setPanelSlideListener(object : DraggablePanel.PanelSlideListener {
			override fun onPanelSlide(view: View, dragOffset: Float) {
				if (inputField.input.text.isNullOrEmpty()) {
					inputField.root.updateWeight(0f + 0.3f * (1 - dragOffset))
					historyContainer.updateWeight(1.0f * dragOffset)
				} else {
					inputField.root.updateWeight(0.3f + 0.3f * (1 - dragOffset))
					historyContainer.updateWeight(0.7f * dragOffset)
				}
			}

			override fun onPanelOpened(view: View) {
				val currentWeight = (inputField.root.layoutParams as LinearLayout.LayoutParams).weight
				val animator = if (inputField.input.text.isNullOrEmpty()) {
					ValueAnimator.ofFloat(currentWeight, 0.0f)
				} else {
					inputField.header.isVisible = true
					ValueAnimator.ofFloat(currentWeight, 0.3f)
				}
				animator.addUpdateListener {
					inputField.root.updateWeight(it.animatedValue as Float)
					historyContainer.updateWeight(1.0f - it.animatedValue as Float)
				}
				animator.start()
				mainActivity.apply {
					title = "History"
					supportActionBar?.setDisplayHomeAsUpEnabled(true)
					addMenuProvider(menuProvider)
				}
			}

			override fun onPanelClosed(view: View) {
				inputField.root.animateWeight(inputField.root.weight(), 1.0f)
				historyContainer.animateWeight(historyContainer.weight(), 0.0f)
				inputField.header.isVisible = false
				mainActivity.apply {
					title = "Calculator"
					supportActionBar?.setDisplayHomeAsUpEnabled(false)
					removeMenuProvider(menuProvider)
				}
			}

		})
	}

	private fun FragmentCalculatorBinding.addMenuProvider() {
		mainActivity.addMenuProvider(object : MenuProvider {
			override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

			override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
				if (menuItem.itemId == android.R.id.home) {
					draggablePanel.smoothPanelClose(300)
					return true
				}
				return false
			}
		})
	}

	private val formulaOnTextSizeChangeListener =
		object : CalculatorEditText.OnTextSizeChangeListener {
			override fun onTextSizeChanged(textView: TextView, oldSize: Float) {
				val textScale = oldSize / textView.textSize
				val translationX = (1 - textScale) * (textView.width / 2 - textView.paddingEnd)
				val translationY = (1 - textScale) * (textView.height / 2 - textView.paddingBottom)
				animatorSetStart(textView, textScale, translationX, translationY)
			}
		}

	private fun animatorSetStart(
		textView: TextView,
		textScale: Float,
		translationX: Float,
		translationY: Float
	) = AnimatorSet().apply {
		playTogether(
			ofFloat(textView, "scaleX", textScale, 1.0f),
			ofFloat(textView, "scaleY", textScale, 1.0f),
			ofFloat(textView, "translationX", translationX, 1.0f),
			ofFloat(textView, "translationY", translationY, 1.0f)
		)
		duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
		interpolator = AccelerateDecelerateInterpolator()
	}.start()


	private val menuProvider = object : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menu.findItem(R.id.delete_history).apply {
				isVisible = true
				setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
			}
		}

		override fun onMenuItemSelected(menuItem: MenuItem) = false

	}


	fun isHistoryPanelOpened() = binding.draggablePanel.isOpen()
	fun closeHistoryPanel() = binding.draggablePanel.smoothPanelClose(300)


	override fun onResume() {
		super.onResume()
//		refreshAd()
	}

	private fun populateNativeAdView(nativeAd: NativeAd, unifiedAdBinding: AdUnifiedBinding) {
		val nativeAdView = unifiedAdBinding.root as NativeAdView

		nativeAdView.iconView = unifiedAdBinding.adAppIcon
		nativeAdView.headlineView = unifiedAdBinding.adHeadline
		nativeAdView.bodyView = unifiedAdBinding.adBody
		nativeAdView.callToActionView = unifiedAdBinding.adCallToAction

		unifiedAdBinding.adHeadline.text = nativeAd.headline
		unifiedAdBinding.adBody.text = nativeAd.body
		unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
		unifiedAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)

		nativeAdView.setNativeAd(nativeAd)
	}

	private fun refreshAd() {
		val builder = AdLoader.Builder(mainActivity, ADMOB_AD_UNIT_ID)

		builder.forNativeAd { nativeAd ->
			if (mainActivity.isDestroyed || mainActivity.isFinishing || mainActivity.isChangingConfigurations) {
				nativeAd.destroy()
				return@forNativeAd
			}

			currentNativeAd?.destroy()
			currentNativeAd = nativeAd
			val unifiedAdBinding = AdUnifiedBinding.inflate(layoutInflater)
			populateNativeAdView(nativeAd, unifiedAdBinding)
			binding.adFrame.apply {
				removeAllViews()
				addView(unifiedAdBinding.root)
			}
		}

		val adLoader = builder.withAdListener(object : AdListener() {
			override fun onAdFailedToLoad(loadAdError: LoadAdError) {
				Logger.e("Failed to load native ad with error - domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}")
			}
		}).build()

		adLoader.loadAd(AdRequest.Builder().build())
	}
}