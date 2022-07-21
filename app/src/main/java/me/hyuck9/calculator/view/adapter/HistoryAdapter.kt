package me.hyuck9.calculator.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import me.hyuck9.calculator.data.db.entity.History
import me.hyuck9.calculator.databinding.ItemHistoryBinding

class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	var data: MutableList<History> = mutableListOf()

	var onExpressionClick: ((History) -> Unit)? = null
	var onAnswerClick: ((History) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
		ViewHolder(
			ItemHistoryBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)


	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
		(holder as ViewHolder).bind(data[position])

	override fun getItemCount() = data.size

	fun updateList(list: List<History>) {
		data.clear()
		data.addAll(list)
		notifyDataSetChanged()
	}

	inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
		fun bind(item: History) {
			binding.apply {
				history = item
				tvDate.isVisible = isVisibleHeader(layoutPosition)
				tvExpression.setOnClickListener { onExpressionClick?.invoke(item) }
				tvAnswer.setOnClickListener { onAnswerClick?.invoke(item) }
			}
		}
	}

	/** 첫번째 아이템이거나 이전 아이템과 날짜가 다른 경우 TRUE 그 외 FALSE */
	fun isVisibleHeader(position: Int) = position == 0 || data[position].createdTimeText != data[position-1].createdTimeText
}