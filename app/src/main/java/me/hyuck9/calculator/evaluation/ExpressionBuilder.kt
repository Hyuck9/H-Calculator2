package me.hyuck9.calculator.evaluation

import android.text.SpannableStringBuilder
import me.hyuck9.calculator.extensions.toExpression

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

		}

		if ((!isEdited || isError) && appendExpr.isNotEmpty()) {
			isEdited = true
			isError = false
		}

//		appendExpr = tokenizer.getLocalizedExpression(appendExpr)
		return super.replace(startIndex, end, appendExpr, 0, appendExpr.length)
	}

}