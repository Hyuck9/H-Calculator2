package me.hyuck9.calculator.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.hyuck9.calculator.data.db.converter.DateConverter
import me.hyuck9.calculator.data.db.dao.HistoryDao
import me.hyuck9.calculator.data.db.entity.History

@Database(
	entities = [History::class],
	version = 1
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

	abstract fun historyDao(): HistoryDao

	companion object {

		private const val DATABASE_NAME = "H-Calc.db"

		fun build(context: Context): AppDatabase =
			Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
				.fallbackToDestructiveMigration()   // TODO: DB 변경이 있을 경우 기존 데이터 삭제 후 재생성 (테스트 용도로 사용)
				.build()
	}
}