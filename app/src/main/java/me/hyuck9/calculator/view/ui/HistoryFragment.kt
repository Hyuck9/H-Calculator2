package me.hyuck9.calculator.view.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.FragmentHistoryBinding
import me.hyuck9.calculator.extensions.observeLiveData
import me.hyuck9.calculator.view.adapter.HistoryAdapter
import me.hyuck9.calculator.view.base.BaseFragment
import me.hyuck9.calculator.view.viewmodel.HistoryViewModel

class HistoryFragment : BaseFragment() {

	private val historyAdapter by lazy { HistoryAdapter() }
	private val historyViewModel by activityViewModels<HistoryViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		return binding<FragmentHistoryBinding>(
			inflater,
			R.layout.fragment_history,
			container
		).apply {
			bindViews()
			addObservers()
		}.root
	}

	private fun FragmentHistoryBinding.bindViews() {
		historyRecyclerView.apply {
			setHasFixedSize(false)
			layoutManager = LinearLayoutManager(context).apply {
				reverseLayout = false
				stackFromEnd = true
			}
			adapter = historyAdapter
		}

	}

	private fun FragmentHistoryBinding.addObservers() {
		observeLiveData(historyViewModel.histories) { histories ->
			historyAdapter.updateList(histories)
		}
	}

}