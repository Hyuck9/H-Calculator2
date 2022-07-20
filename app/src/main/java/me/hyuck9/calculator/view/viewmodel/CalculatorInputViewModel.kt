package me.hyuck9.calculator.view.viewmodel

import android.app.Application
import android.text.SpannableStringBuilder
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.hyuck9.calculator.common.Constants.BUFFER_1
import me.hyuck9.calculator.common.Constants.BUFFER_2
import me.hyuck9.calculator.common.Constants.BUFFER_3
import me.hyuck9.calculator.common.Constants.BUFFER_4
import me.hyuck9.calculator.common.Constants.CHAR_OF_EMPTY_EXPRESSION
import me.hyuck9.calculator.data.datastore.readString
import me.hyuck9.calculator.data.datastore.writeString
import me.hyuck9.calculator.data.db.entity.History
import me.hyuck9.calculator.evaluation.ExpressionBuilder
import me.hyuck9.calculator.extensions.*
import me.hyuck9.calculator.model.CalculateState
import org.mariuszgromada.math.mxparser.Expression
import timber.log.Timber

class CalculatorInputViewModel(val context: Application) : AndroidViewModel(context) {

	private val numRegex = "-?[0-9]+\\.?[0-9]*".toRegex()
	private var currentState = CalculateState.INPUT

	private val expressionBuilder = MutableLiveData(ExpressionBuilder())
	val expressionLiveData: LiveData<ExpressionBuilder> = expressionBuilder

	private val inputMutableLiveData = MutableLiveData("")
	val inputLiveData: LiveData<String> = inputMutableLiveData

	private val outputMutableLiveData = MutableLiveData("")
	val outputLiveData: LiveData<String> = outputMutableLiveData

	val memory1 = context.readString(BUFFER_1).asLiveData()
	val memory2 = context.readString(BUFFER_2).asLiveData()
	val memory3 = context.readString(BUFFER_3).asLiveData()
	val memory4 = context.readString(BUFFER_4).asLiveData()


	fun setMockMemory() {
		viewModelScope.launch {
			context.writeString(key = BUFFER_1, value = "1")
			context.writeString(key = BUFFER_2, value = "12")
			context.writeString(key = BUFFER_3, value = "123")
			context.writeString(key = BUFFER_4, value = "1234")
		}
	}

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

		setExpression()
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

	fun equalClicked(saveHistory: (history: History)-> Unit) {
		if (outputMutableLiveData.value.isNullOrEmpty()) {
			// TODO: Toast Message - 수식이 잘못되었습니다.
			return
		}

//		inputMutableLiveData.value = inputMutableLiveData.value!!.maybeAppendClosedBrackets()

		if (currentState == CalculateState.INPUT) {
			// TODO: setDisplayState(CalculatorState.EVALUATE) -> Display 상태별 글자 색 변경
			onEvaluate(saveHistory)
		}

	}

	private fun onEvaluate(saveHistory: (history: History)-> Unit) {
		val currentInput = expressionBuilder.value!!
		if (currentInput.isNotEmpty()) {
			Timber.i("currentInput : $currentInput")
			if ("×÷−+".contains(currentInput.last())) {
				Timber.i("last : ${currentInput.last()}")
				// TODO: Toast Message - 완성되지 않은 수식입니다.
				return
			}
			val result = Expression(expressionBuilder.value!!.toExpression().maybeAppendClosedBrackets()).calculate()
			val history = History(expressionBuilder.value!!.maybeAppendClosedBrackets(), result.toSimpleString())
			saveHistory.invoke(history)
			setExpression(result.toSimpleString())
		}
	}




	fun calculateOutput() {
		val currentInput = expressionBuilder.value!!
		if (currentInput.matches(numRegex)) {
			outputMutableLiveData.value = ""
		} else if (currentInput.isNotEmpty()) {
			val result = Expression(expressionBuilder.value!!.toExpression().maybeAppendClosedBrackets()).calculate()
			if (result.isNaN() || result.isInfinite()) {
				// TODO: 오류 표시
			} else {
				outputMutableLiveData.value = result.toSimpleString().makeCommaExpr()
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


	fun setExpression(expr: String = "") {
		expressionBuilder.value = ExpressionBuilder(expr)
	}



	private fun appendExpression(input: String) {
		expressionBuilder.value!!.append(input).setExpressionBuilder()
	}




	private fun SpannableStringBuilder.setExpressionBuilder() {
		expressionBuilder.value = this as ExpressionBuilder
	}

}