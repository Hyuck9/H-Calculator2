package me.hyuck9.calculator.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.FragmentCalculatorBinding
import me.hyuck9.calculator.view.base.BaseFragment

class CalculatorFragment : BaseFragment() {


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

		}.root
	}
}