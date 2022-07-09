package me.hyuck9.calculator.view.viewmodel

import android.text.SpannableStringBuilder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.hyuck9.calculator.common.Constants.CHAR_OF_EMPTY_EXPRESSION
import me.hyuck9.calculator.evaluation.ExpressionBuilder
import me.hyuck9.calculator.extensions.*
import me.hyuck9.calculator.model.CalculateState
import org.mariuszgromada.math.mxparser.Expression
import timber.log.Timber

class CalculatorInputViewModel : ViewModel() {

	private val numRegex = "-?[0-9]+\\.?[0-9]*".toRegex()
	private var currentState = CalculateState.INPUT

	private val expressionBuilder = MutableLiveData(ExpressionBuilder())
	val expressionLiveData: LiveData<ExpressionBuilder> = expressionBuilder

	private val inputMutableLiveData = MutableLiveData("")
	val inputLiveData: LiveData<String> = inputMutableLiveData

	private val outputMutableLiveData = MutableLiveData("")
	val outputLiveData: LiveData<String> = outputMutableLiveData



	fun setViewExpression() {
		inputMutableLiveData.value = expressionBuilder.value!!.makeCommaExpr()
	}





	fun numberClicked(num: String) {
		// TODO: 글자수 제한 체크
		if (isLastInputRightParen() && isStateNotResult()) {
			appendExpression("×")
		}

		if (num == "00") {
			appendDoubleO()
		} else {
			appendExpression(num)
		}
	}

	fun operatorClicked(oper: String) {
		// TODO: 에러 체크
		// TODO: 글자수 제한 체크
		appendExpression(oper)
	}

	fun parensClicked() {
		// TODO: 글자수 제한 체크
		appendParens()
	}

	fun clearAll(): Boolean {
		if (expressionBuilder.value.isNullOrBlank()) return false

		expressionBuilder.value = ExpressionBuilder()
		outputMutableLiveData.value = ""
		return true
	}

	fun backspaceClicked() {
		with(expressionBuilder.value!!) {
			isError = false
			// TODO: isError .....
			// TODO: isEqual .....
			// TODO: memory .....
			if (isNotEmpty()) {
				delete(length - 1, length).setExpressionBuilder()
			}
		}
	}



	fun calculateOutput() {
		val currentInput = expressionBuilder.value!!
		if (currentInput.matches(numRegex)) {
			outputMutableLiveData.value = ""
		} else if (currentInput.isNotEmpty()) {
			val result = Expression(expressionBuilder.value!!.toExpression()).calculate()
			if (result.toString() != "NaN") {
				outputMutableLiveData.value = result.toSimpleString()
			}
		} else {
			outputMutableLiveData.value = ""
		}
	}





	private fun appendParens() {
		if (isMoreThanLeftParen() && checkLastOperatorAndLeftParen().not()) {
			appendExpression(")")
		} else if (expressionBuilder.value!!.isEdited && checkLastOperatorAndLeftParen().not()) {
			appendExpression("×(")
		} else {
			appendExpression("(")
		}
	}
	private fun isMoreThanLeftParen() = expressionBuilder.value!!.run {
		return@run getLeftParenCount() - getRightParenCount() > 0
	}
	private fun getLastInput() = expressionBuilder.value!!.lastOrNull() ?: CHAR_OF_EMPTY_EXPRESSION
	private fun checkLastOperatorAndLeftParen() = "s(×÷−+".contains(getLastInput())
	private fun isLastInputRightParen() = ")".contains(getLastInput())




	private fun appendDoubleO() {
		if (isFirstCharAtOperandZero()) {
			Timber.i("First char at operand is Zero")
		} else if (isFirstOperandOrEmpty()) {
			appendExpression("0")
		} else {
			appendExpression("00")
		}
	}
	private fun isFirstCharAtOperandZero() = if (expressionBuilder.value!!.length >= 2) {
		"×÷−+".contains(expressionBuilder.value!!.run{this[length - 2]}) && getLastInput() == '0'
	} else {
		expressionBuilder.value!!.singleOrNull() == '0'
	}
	private fun isFirstOperandOrEmpty() = "s×÷−+".contains(getLastInput())



	private fun isStateResult() = currentState == CalculateState.RESULT
	private fun isStateNotResult() = isStateResult().not()




	fun isEdited() = currentState == CalculateState.INPUT || currentState == CalculateState.ERROR
	fun setInputState() {
		currentState = CalculateState.INPUT
	}




	private fun appendExpression(input: String) {
		expressionBuilder.value!!.append(input).setExpressionBuilder()
	}




	private fun SpannableStringBuilder.setExpressionBuilder() {
		expressionBuilder.value = this as ExpressionBuilder
	}

}