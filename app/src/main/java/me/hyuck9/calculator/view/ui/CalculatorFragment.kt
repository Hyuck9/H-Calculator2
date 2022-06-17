package me.hyuck9.calculator.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.FragmentCalculatorBinding
import me.hyuck9.calculator.extensions.observeLiveData
import me.hyuck9.calculator.view.base.BaseFragment
import me.hyuck9.calculator.view.viewmodel.CalculatorInputViewModel

class CalculatorFragment : BaseFragment() {

	private val calcViewModel: CalculatorInputViewModel by viewModels()

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
			addObservers()
		}.root
	}

	private fun addObservers() {
		observeLiveData(calcViewModel.inputLiveData) {
			calcViewModel.calculateOutput()
		}
	}
}