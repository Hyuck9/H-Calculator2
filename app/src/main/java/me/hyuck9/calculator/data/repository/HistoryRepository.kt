package me.hyuck9.calculator.data.repository

import me.hyuck9.calculator.data.db.dao.HistoryDao
import me.hyuck9.calculator.data.db.entity.History
import javax.inject.Inject

class HistoryRepository @Inject constructor(
	private val historyDao: HistoryDao
) : Repository {

	val allHistories = historyDao.getHistories()

	suspend fun insert(history: History) {
		historyDao.insert(history)
	}

	suspend fun delete(history: History) {
		historyDao.delete(history)
	}

	suspend fun deleteAll() {
		historyDao.deleteAll()
	}

}