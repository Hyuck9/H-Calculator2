package me.hyuck9.calculator.extensions

import android.text.Spanned
import androidx.core.text.HtmlCompat


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