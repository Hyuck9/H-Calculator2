package me.hyuck9.calculator.extensions

import java.math.BigDecimal

fun String.toExpression(): String {
	return replace('×', '*')
		.replace('÷', '/')
		.replace('−', '-')
		.replace("π", "pi")
		.replace("sin⁻¹", "asin")
		.replace("cos⁻¹", "acos")
		.replace("tan⁻¹", "atan")
		.replace("log", "log10")
		.maybeAppendClosedBrackets()
}

fun String.maybeAppendClosedBrackets(): String {
	var expression = this
	val open = getLeftParentCount()
	val close = getRightParentCount()

	if (open - close > 0) {
		for (i in 0 until open - close) expression += ")"
	} else if (close - open > 0) {
		for (i in 0 until close - open) expression = "($expression"
	}

	return expression
}

fun String.getLeftParentCount() = getCharCountOfString('(')

fun String.getRightParentCount() = getCharCountOfString(')')

fun String.getCharCountOfString(find: Char): Int {
	var count = 0
	for (element in this) {
		if (element == find) {
			count++
		}
	}
	return count
}

fun Double.toSimpleString(): String {
	return toBigDecimal().toSimpleString()
}

fun BigDecimal.toSimpleString(): String {
	return toPlainString().removeSuffix(".0")
}