package me.hyuck9.calculator.view.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.hyuck9.calculator.data.db.entity.History
import me.hyuck9.calculator.data.repository.HistoryRepository
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
	private val historyRepository: HistoryRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel() {

	val histories = historyRepository.allHistories

	fun insertHistory(history: History) = viewModelScope.launch {
		historyRepository.insert(history)
	}
}