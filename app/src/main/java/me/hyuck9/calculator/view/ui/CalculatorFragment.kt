package me.hyuck9.calculator.view.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator.ofFloat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.viewModels
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.FragmentCalculatorBinding
import me.hyuck9.calculator.extensions.observeLiveData
import me.hyuck9.calculator.view.base.BaseFragment
import me.hyuck9.calculator.view.custom.CalculatorEditText
import me.hyuck9.calculator.view.viewmodel.CalculatorInputViewModel

class CalculatorFragment : BaseFragment() {

	private val calcViewModel by viewModels<CalculatorInputViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		return binding<FragmentCalculatorBinding>(
			inflater,
			R.layout.fragment_calculator,
			container
		).apply {
			viewModel = calcViewModel
			bindViews()
			addObservers()
		}.root
	}

	private fun FragmentCalculatorBinding.bindViews() {
		inputField.input.setOnTextSizeChangeListener(formulaOnTextSizeChangeListener)
	}

	private fun addObservers() {
		observeLiveData(calcViewModel.inputLiveData) {
			calcViewModel.calculateOutput()
		}
		observeLiveData(calcViewModel.expressionLiveData) {
			calcViewModel.setViewExpression()
		}
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

}