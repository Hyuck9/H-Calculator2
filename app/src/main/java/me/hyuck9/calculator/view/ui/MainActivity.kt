package me.hyuck9.calculator.view.ui

import android.os.Bundle
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.ActivityMainBinding
import me.hyuck9.calculator.view.base.BaseActivity

@AndroidEntryPoint
class MainActivity : BaseActivity() {

	private val binding: ActivityMainBinding by binding(R.layout.activity_main)
	private val calculatorFragment = CalculatorFragment()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding.apply {
			setUpViews()
		}
	}

	private fun setUpViews() {
		supportFragmentManager.commit {
			add(R.id.fcv_container, calculatorFragment, calculatorFragment.javaClass.name)
		}
	}

	override fun onBackPressed() {

		if (calculatorFragment.isHistoryPanelOpened()) {
			calculatorFragment.closeHistoryPanel()
			return
		}

		super.onBackPressed()
	}
}