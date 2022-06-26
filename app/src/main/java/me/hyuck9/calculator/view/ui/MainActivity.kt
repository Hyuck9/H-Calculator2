package me.hyuck9.calculator.view.ui

import android.os.Bundle
import androidx.fragment.app.commit
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.ActivityMainBinding
import me.hyuck9.calculator.view.base.BaseActivity

class MainActivity : BaseActivity() {

	private val binding: ActivityMainBinding by binding(R.layout.activity_main)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding.apply {
			setUpViews()
		}
	}

	private fun setUpViews() {
		supportFragmentManager.commit {
			val fragment = CalculatorFragment()
			add(R.id.fcv_container, fragment, fragment.javaClass.name)
		}
	}
}