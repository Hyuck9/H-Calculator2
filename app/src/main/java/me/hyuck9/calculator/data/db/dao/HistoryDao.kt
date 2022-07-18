package me.hyuck9.calculator.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import me.hyuck9.calculator.data.db.entity.History

@Dao
interface HistoryDao {

	@Query("SELECT * FROM History")
	fun getHistories(): LiveData<List<History>>

	@Insert
	suspend fun insert(history: History)

	@Delete
	suspend fun delete(history: History)

	@Query("DELETE FROM History")
	suspend fun deleteAll()
}