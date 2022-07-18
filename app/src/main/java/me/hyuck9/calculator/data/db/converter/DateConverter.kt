package me.hyuck9.calculator.data.db.converter

import androidx.room.TypeConverter
import org.threeten.bp.LocalDateTime

class DateConverter {

	@TypeConverter
	fun toLocalDateTime(dateString: String?): LocalDateTime? =
		dateString?.let {
			LocalDateTime.parse(it)
		}

	@TypeConverter
	fun toTimeStamp(date: LocalDateTime?): String? =
		date?.let {
			date.toString()
		}

}