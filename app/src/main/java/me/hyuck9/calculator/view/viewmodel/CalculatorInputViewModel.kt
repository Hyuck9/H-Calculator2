package me.hyuck9.calculator.view.viewmodel

import android.app.Application
import android.text.SpannableStringBuilder
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.hyuck9.calculator.R
import me.hyuck9.calculator.common.Constants.BUFFER
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
import splitties.resources.appStr
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

	private val _toastMessage = MutableLiveData("")
	val toastMessage: LiveData<String> = _toastMessage



	val memory = arrayOf(
		context.readString(BUFFER_1).asLiveData(),
		context.readString(BUFFER_2).asLiveData(),
		context.readString(BUFFER_3).asLiveData(),
		context.readString(BUFFER_4).asLiveData()
	)


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

	fun equalClicked(saveHistory: (history: History) -> Unit) {
		if (outputMutableLiveData.value.isNullOrEmpty()) {
			_toastMessage.value = appStr(R.string.error_formula_is_wrong)
			return
		}

		if (currentState == CalculateState.INPUT) {
			// TODO: setDisplayState(CalculatorState.EVALUATE) -> Display 상태별 글자 색 변경
			onEvaluate(saveHistory)
		}

	}

	private fun onEvaluate(saveHistory: (history: History) -> Unit) {
		var currentInput = expressionBuilder.value!!.toString()
		val output = outputMutableLiveData.value!!.removeComma()
		if (currentInput.isNotEmpty()) {
			Timber.i("currentInput : $currentInput")
			if ("×÷−+".contains(currentInput.last())) {
				currentInput = currentInput.substring(0, currentInput.length - 1)
			}
			setExpression(output.toViewExpression())
			if (currentInput.isOperatorInExpr()) {
				val history = History(currentInput.maybeAppendClosedBrackets(), output)
				saveHistory.invoke(history)
			}
		}
	}


	fun calculateOutput() {
		val currentInput = expressionBuilder.value!!
		if (currentInput.isNotEmpty()) {
			if ("×÷−+".contains(currentInput.last())) {
				return
			}
			val result = Expression(
				expressionBuilder.value!!.toExpression().maybeAppendClosedBrackets()
			).calculate()
			if (result.isNaN() || result.isInfinite()) {
				outputMutableLiveData.value = ""
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
		"×÷−+".contains(expressionBuilder.value!!.run { this[length - 2] }) && getLastInput() == '0'
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


	fun saveBuffer(index: Int) {
		if (memory[index].value.isNullOrEmpty()) {    // 버퍼가 비어있는 경우 - 저장
			if (expressionBuilder.value!!.isOperatorNotInExpr()) {    // 계산식에 연산자가 없을 때에만 버퍼에 저장
				if (expressionBuilder.value.isNullOrEmpty()) {
					_toastMessage.value = appStr(R.string.error_nothing_to_save)
				} else if (expressionBuilder.value!!.isError || currentState == CalculateState.ERROR) {
					_toastMessage.value = appStr(R.string.error_cannot_save)
				} else {    // 버퍼 저장
					viewModelScope.launch {
						context.writeString(
							key = "$BUFFER${index + 1}",
							value = expressionBuilder.value!!.toExpression()
						)
					}
				}
			} else {    // 계산식 저장 불가
				_toastMessage.value = appStr(R.string.error_formula_cannot_save)
			}
		} else {    // 버퍼에 값이 있는 경우 - 값 불러오기
			var bufferedValue = memory[index].value!!
			if (bufferedValue.contains("-")) bufferedValue = "($bufferedValue)"
			appendBufferedValue(bufferedValue)
		}
	}

	private fun appendBufferedValue(bufferedValue: String) {
		if (checkLastOperatorAndLeftParen()) {
			appendExpression(bufferedValue)
		} else {
			_toastMessage.value = appStr(R.string.error_operator_first)
		}
	}

	fun deleteMemoryBuffer(index: Int): Boolean {
		viewModelScope.launch {
			context.writeString(
				key = "$BUFFER${index + 1}",
				value = ""
			)
		}
		return true
	}

	fun clearMemory() = viewModelScope.launch {
		context.writeString(key = BUFFER_1, value = "")
		context.writeString(key = BUFFER_2, value = "")
		context.writeString(key = BUFFER_3, value = "")
		context.writeString(key = BUFFER_4, value = "")
		_toastMessage.value = appStr(R.string.message_all_memory_clear)
	}


	private fun appendExpression(input: String) {
		expressionBuilder.value!!.append(input).setExpressionBuilder()
	}

	fun setExpression(expr: String = "") {
		expressionBuilder.value = ExpressionBuilder(expr)
	}


	private fun SpannableStringBuilder.setExpressionBuilder() {
		expressionBuilder.value = this as ExpressionBuilder
	}

}