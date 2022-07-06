package me.hyuck9.calculator.extensions

import androidx.core.text.isDigitsOnly
import java.math.BigDecimal
import java.text.DecimalFormat

val operators = listOf('^', '÷', '×', '+', '−', '(', ')')

fun CharSequence.toViewExpression(): String {
	return toString()
		.replace('*', '×')
		.replace('/', '÷')
		.replace('-', '−')
		.replace("pi", "π")
		.replace("asin", "sin⁻¹")
		.replace("acos", "cos⁻¹")
		.replace("atan", "tan⁻¹")
		.replace("log10", "log")
}

fun CharSequence.toExpression(): String {
	return toString()
		.replace('×', '*')
		.replace('÷', '/')
		.replace('−', '-')
		.replace("π", "pi")
		.replace("sin⁻¹", "asin")
		.replace("cos⁻¹", "acos")
		.replace("tan⁻¹", "atan")
		.replace("log", "log10")
		.removeComma()
		.maybeAppendClosedBrackets()
}




fun String.addComma(): String {
	val strNumber: String
	var strDecimal = ""
	if (contains(".")) {
		strNumber = substring(0, indexOf("."))
		strDecimal = substring(indexOf("."), length)
	} else {
		strNumber = this
	}
	return DecimalFormat("#,###").format(strNumber.removeCommaToBigInteger()) + strDecimal
}
fun String.removeComma() = this.replace(",", "")
fun String.removeCommaToBigInteger() = this.removeComma().toBigInteger()
fun String.removeCommaToDouble() = this.removeComma().toDouble()
fun String.makeCommaExpr(): String {
	var exprString = ""
	val array = separateOperator()

	for (str in array) {
		val s = str.replace(".", "")
		exprString += if (s.isDigitsOnly()) str.addComma() else str
	}
	return exprString
}




fun String.separateOperator(): ArrayList<String> {
	val array = arrayListOf<String>()
	var offset = 0

	forEachIndexed { index, char ->
		if (operators.contains(char)) {	// 이번 char가 operator인 경우
			if (substring(offset, index).isNumeric()) {	// 이번 char 전까지의 문자열이 숫자라면
				array.add(substring(offset, index))					// 숫자 저장
			}
			if ("()".contains(char)) {
				array.add(char.toString())							// 괄호 저장
			} else {
				array.add("<font color='#1b80ef'> $char </font>")	// operator 저장
			}
			offset = index + 1	// 다음 operator까지 자를 시작 지점 저장
		}
	}
	if (offset != length) {						// 계산식 마지막에 숫자가 있는 경우
		array.add(substring(offset, length))	// 숫자 저장
	}
	return array
}

fun String.isNumeric(): Boolean {
	if (isNotEmpty()) {
		for (c in this) {
			if (c.isDigit().not() && ".E".contains(c).not()) {
				return false
			}
		}
	} else {
		return false
	}
	return true
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