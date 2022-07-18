package me.hyuck9.calculator.extensions

import android.text.Spanned
import androidx.core.text.HtmlCompat
import me.hyuck9.calculator.R
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import splitties.resources.appStr


fun String.fromHtml(): Spanned {
	return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
}

fun String.myCharContains(str: String): Boolean {
	for (c in this) {
		if (str.contains(c)) return true
	}
	return false
}

fun String.containsWords(vararg words: String): Boolean {
	for (word in words) {
		if (this.contains(word)) {
			return true
		}
	}
	return false
}

fun LocalDateTime.toTimeString(): String {
	val today = LocalDateTime.now()
	val diffDays = ChronoUnit.DAYS.between(this.toLocalDate(), today.toLocalDate()).toInt()
	val diffHour = ChronoUnit.HOURS.between(this, today).toInt()
	val diffMin = ChronoUnit.MINUTES.between(this, today).toInt()
	return when {
		diffDays > 30 -> this.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
		diffDays > 1 -> appStr(R.string.text_days, diffDays)
		diffDays == 1 -> appStr(R.string.text_yesterday)
		diffHour > 1 -> this.format(DateTimeFormatter.ofPattern("a HH:mm"))
		diffHour == 1 -> appStr(R.string.text_hours, diffHour)
		diffMin > 0 -> appStr(R.string.text_minutes, diffMin)
		else -> appStr(R.string.text_just_now)
	}
}

fun LocalDateTime.toDateString(): String {
	val today = LocalDateTime.now()
	val diffDays = ChronoUnit.DAYS.between(this.toLocalDate(), today.toLocalDate()).toInt()
	return when {
		diffDays > 30 -> this.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
		diffDays > 1 -> appStr(R.string.text_days, diffDays)
		diffDays == 1 -> appStr(R.string.text_yesterday)
		else -> appStr(R.string.text_today)
	}
}