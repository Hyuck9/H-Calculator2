package me.hyuck9.calculator.extensions

import androidx.core.text.isDigitsOnly
import java.math.BigDecimal
import java.text.DecimalFormat

val operators = listOf('÷', '×', '+', '−', '(', ')')

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
}




fun String?.addComma(): String {
	if (isNullOrEmpty()) return ""
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
fun CharSequence.makeCommaExpr(): String {
	var exprString = ""
	val array = separateOperator()

	for (str in array) {
		val s = str.replace(".", "")
		exprString += if (s.isDigitsOnly()) str.addComma() else str
	}
	return exprString
}
fun CharSequence.checkOperandMaxLength(): Boolean {
	val array = separateSimpleOperator()
	return array.last().length < 15
}
fun CharSequence.checkDouble0OperandMaxLength(): Boolean {
	val array = separateSimpleOperator()
	return array.last().length >= 14
}
fun CharSequence.checkExprMaxLength(): Boolean = length < 200



fun CharSequence.separateSimpleOperator(): ArrayList<String> {
	val array = arrayListOf<String>()
	var offset = 0
	forEachIndexed { index, char ->
		if (operators.contains(char)) {    // 이번 char가 operator인 경우
			if (substring(offset, index).isNumeric()) {    // 이번 char 전까지의 문자열이 숫자라면
				array.add(substring(offset, index))        // 숫자 저장
			}
			offset = index + 1                            // 다음 operator까지 자를 시작 지점 저장
		}
	}
	if (offset != length) {							// 계산식 마지막에 숫자가 있는 경우
		array.add(substring(offset, length))		// 숫자 저장
	} else {
		array.add("")
	}
	return array
}
fun CharSequence.separateOperator(): ArrayList<String> {
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
				array.add("<font color='#4CAF50'>$char</font>")	// operator 저장
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

fun CharSequence.maybeAppendClosedBrackets(): String {
	var expression = this.toString()
	val open = getLeftParenCount()
	val close = getRightParenCount()

	if (open - close > 0) {
		for (i in 0 until open - close) expression += ")"
	} else if (close - open > 0) {
		for (i in 0 until close - open) expression = "($expression"
	}

	return expression
}

fun CharSequence.getLeftParenCount() = getCharCountOfString('(')

fun CharSequence.getRightParenCount() = getCharCountOfString(')')

fun CharSequence.getCharCountOfString(find: Char): Int {
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

fun Double.isOverLimit(): Boolean {
	return toBigDecimal().abs() > 9007199254740991.0.toBigDecimal()
}

fun BigDecimal.toSimpleString(): String {
	return toPlainString().removeSuffix(".0")
}


fun CharSequence.isOperatorInExpr(): Boolean {
	val expr = this.toExpression()
	if (expr.isNotEmpty()) {
		if (expr[0] == '-') {
			for (i in 1 until expr.length) {
				if ("+-*/()".indexOf(expr[i]) != -1) return true
			}
		} else {
			for (element in expr) {
				if ("+-*/()".indexOf(element) != -1) return true
			}
		}
	}
	return false
}
fun CharSequence.isOperatorNotInExpr() = isOperatorInExpr().not()