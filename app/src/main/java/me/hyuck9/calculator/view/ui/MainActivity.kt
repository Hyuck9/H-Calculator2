package me.hyuck9.calculator.view.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.ActivityMainBinding
import me.hyuck9.calculator.view.base.BaseActivity
import me.hyuck9.calculator.view.viewmodel.HistoryViewModel
import splitties.resources.str

@AndroidEntryPoint
class MainActivity : BaseActivity() {

	private val binding: ActivityMainBinding by binding(R.layout.activity_main)
	private val calculatorFragment: CalculatorFragment by lazy {
		supportFragmentManager.findFragmentById(R.id.fcv_container)!!
			.childFragmentManager
			.fragments[0] as CalculatorFragment
	}
	private val historyViewModel by viewModels<HistoryViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding.apply {
			bindViews()
		}
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
			return
		}

		super.onBackPressed()
	}


	private fun showDeleteHistoryDialog() {
		MaterialAlertDialogBuilder(this)
			.setTitle(str(R.string.dialog_title_delete))
			.setMessage(str(R.string.dialog_message_delete))
			.setIcon(R.drawable.ic_delete_history)
			.setPositiveButton(android.R.string.ok) { _, _ ->
				historyViewModel.deleteAllHistory()
			}
			.setNegativeButton(android.R.string.cancel) { dialog, _ ->
				dialog.dismiss()
			}
			.create()
			.show()
	}
}