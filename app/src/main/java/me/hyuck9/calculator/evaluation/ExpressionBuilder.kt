package me.hyuck9.calculator.evaluation

import android.text.SpannableStringBuilder
import androidx.core.text.isDigitsOnly
import me.hyuck9.calculator.extensions.toExpression
import me.hyuck9.calculator.extensions.toViewExpression

class ExpressionBuilder(text: CharSequence, var isEdited: Boolean = false) : SpannableStringBuilder(text) {

	var isError = false

	override fun replace(
		start: Int,
		end: Int,
		tb: CharSequence,
		tbstart: Int,
		tbend: Int
	): SpannableStringBuilder {
		if (start != length || end != length) {
			isEdited = true
			return super.replace(start, end, tb, tbstart, tbend)
		}
		var startIndex = start
		var appendExpr = tb.subSequence(tbstart, tbend).toExpression()

		if (appendExpr.length == 1) {
			val expr = toString().toExpression()

			when (appendExpr[0]) {
				'.' -> {
					// 하나의 피연산자에 하나의 소수점만 가능하도록
					val index = expr.lastIndexOf('.')
					if (index != -1 && expr.substring(index + 1, startIndex).isDigitsOnly()) {
						appendExpr = ""
					}
					if (startIndex == 0 ||											// 앞에 숫자가 없을 때
						startIndex > 0 && "+-*/(".contains(expr[startIndex - 1]) ||	// 마지막 입력값이 operator일 때
						isEdited.not() && appendExpr.isNotEmpty() ||				// 계산 완료 후 바로 입력 시
						isError														// 전에 계산 결과가 Error 였을 때 (처음부터 재입력)
					) {
						appendExpr = "0."
					}
				}

				'+','*','/' -> {
					// 아무 입력이 없는 처음 상태에선 주요 연산자를 쓰지 못하도록
					if (startIndex == 0) {
						appendExpr = ""
					} else if (startIndex == 1 && "-".contains(expr[0])) {
						appendExpr = ""
					}
					if (startIndex > 0 && "(".contains(expr[startIndex - 1])) {
						appendExpr = ""
					}

					// 연속적인 연산자 사용 불허
					while (startIndex > 0 && "+-*/".contains(expr[startIndex - 1])) {
						--startIndex
					}
					isEdited = true
				}

				'-' -> {
					// -- or +- 불허
					if (startIndex > 0 && "+-".contains(expr[startIndex - 1])) {
						--startIndex
					}

					if (isError) {
						appendExpr = ""
					}
					isEdited = true
				}

				'(', ')' -> {}

				else -> {
					// 처음 0 중복 입력 불가, 첫 숫자 0 이후 다른 숫자 눌를 시 덮어쓰기
					if (expr == "0") {
						--startIndex
					}
					// operator 이후 처음 0 중복 입력 불가, operator 이후 첫 숫자 0 이후 다른 숫자 눌를 시 덮어쓰기
					if (startIndex > 1 && "+/-*".contains(expr[startIndex - 2]) && expr[startIndex - 1] == '0') {
						--startIndex
					}
				}
			}
		}

		if ((!isEdited || isError) && appendExpr.isNotEmpty()) {
			isEdited = true
			isError = false
		}

		appendExpr = appendExpr.toViewExpression()
		return super.replace(startIndex, end, appendExpr, 0, appendExpr.length)
	}

}