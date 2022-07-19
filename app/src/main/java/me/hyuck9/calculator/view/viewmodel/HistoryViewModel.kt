package me.hyuck9.calculator.view.viewmodel

import androidx.lifecycle.*
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

	private val selectedHistoryMutableLiveData: MutableLiveData<String> = MutableLiveData()
	val selectedHistory: LiveData<String> = selectedHistoryMutableLiveData

	fun selectHistory(expr: String) {
		selectedHistoryMutableLiveData.value = expr
	}

	fun insertHistory(history: History) = viewModelScope.launch {
		historyRepository.insert(history)
	}

	fun deleteHistory(history: History) =
		viewModelScope.launch {
			historyRepository.delete(history)
		}

}