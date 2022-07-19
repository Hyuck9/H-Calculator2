package me.hyuck9.calculator.view.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator.ofFloat
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.hyuck9.calculator.R
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

	private val calcViewModel by viewModels<CalculatorInputViewModel>()
	private val historyViewModel by activityViewModels<HistoryViewModel>()

	private val historyFragment = HistoryFragment()

	private lateinit var binding: FragmentCalculatorBinding

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
				// TODO: 액티비티 액션바 handling
			}

			override fun onPanelClosed(view: View) {
				inputField.root.animateWeight(inputField.root.weight(), 1.0f)
				historyContainer.animateWeight(historyContainer.weight(), 0.0f)
				inputField.header.isVisible = false
				// TODO: 액티비티 액션바 handling
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



	fun isHistoryPanelOpened() = binding.draggablePanel.isOpen()
	fun closeHistoryPanel() = binding.draggablePanel.smoothPanelClose(300)

}