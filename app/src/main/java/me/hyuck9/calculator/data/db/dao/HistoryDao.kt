package me.hyuck9.calculator.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import me.hyuck9.calculator.data.db.entity.History

@Dao
interface HistoryDao {

	@Query("SELECT * FROM History")
	fun getHistories(): LiveData<List<History>>

	@Insert
	suspend fun insert(history: History)

	@Transaction
	@Delete
	suspend fun delete(history: History)

	@Transaction
	@Query("DELETE FROM History")
	suspend fun deleteAll()
}