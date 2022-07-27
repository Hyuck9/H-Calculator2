package me.hyuck9.calculator.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.hyuck9.calculator.R
import me.hyuck9.calculator.databinding.FragmentHistoryBinding
import me.hyuck9.calculator.extensions.observeLiveData
import me.hyuck9.calculator.extensions.toViewExpression
import me.hyuck9.calculator.view.adapter.HistoryAdapter
import me.hyuck9.calculator.view.base.BaseFragment
import me.hyuck9.calculator.view.viewmodel.HistoryViewModel
import timber.log.Timber

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
			bindListeners()
			addObservers()
		}.root
	}

	private fun FragmentHistoryBinding.bindViews() {
		rvHistory.apply {
			setHasFixedSize(false)
			layoutManager = LinearLayoutManager(context).apply {
				reverseLayout = false
				stackFromEnd = true
			}
			adapter = historyAdapter
		}
	}

	private fun FragmentHistoryBinding.bindListeners() {
		ItemTouchHelper(object :
			ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(
				recyclerView: RecyclerView,
				viewHolder: RecyclerView.ViewHolder,
				target: RecyclerView.ViewHolder
			): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val position = viewHolder.adapterPosition
				historyViewModel.deleteHistory(historyAdapter.data[position])
			}
		}).attachToRecyclerView(rvHistory)

		historyAdapter.onExpressionClick = { history ->
			historyViewModel.selectHistory(history.expr)
		}
		historyAdapter.onAnswerClick = { history ->
			historyViewModel.selectHistory(history.answer.toViewExpression())
		}

	}

	private fun FragmentHistoryBinding.addObservers() {
		observeLiveData(historyViewModel.histories) { histories ->
			noHistory.isVisible = histories.isEmpty()
			historyAdapter.updateList(histories)
		}
	}

}