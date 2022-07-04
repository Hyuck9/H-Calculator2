package me.hyuck9.calculator.extensions


fun String.containsWords(vararg words: String): Boolean {
	if (this.isEmpty()) return false
	for (word in words) {
		if (this.contains(word)) {
			return true
		}
	}
	return false
}