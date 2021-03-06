package me.hyuck9.calculator.view.viewmodel

import android.app.Application
import android.text.SpannableStringBuilder
import androidx.annotation.StringRes
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
import org.javia.arity.Symbols
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

	var isOverLimit = false


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
		if (expressionBuilder.value!!.checkExprMaxLength()) {
			if (expressionBuilder.value!!.checkOperandMaxLength() || currentState == CalculateState.RESULT) {
				if (isLastInputRightParen() && isStateNotResult()) {
					appendExpression("??")
				}

				if (num == "00") {
					appendDouble0()
				} else {
					appendExpression(num)
				}
			} else {
				showToastMessage(R.string.error_maximum_number)
			}
		} else {
			showToastMessage(R.string.error_maximum_character)
		}
	}

	fun operatorClicked(oper: String) {
		if (expressionBuilder.value!!.isError) {
			Timber.i("Expression is Error => [Clear Expression]")
			setExpression()
			return
		}
		if (expressionBuilder.value!!.checkExprMaxLength()) {
			appendExpression(oper)
		} else {
			showToastMessage(R.string.error_maximum_character)
		}
	}

	fun parensClicked() {
		if (expressionBuilder.value!!.checkExprMaxLength()) {
			appendParens()
		} else {
			showToastMessage(R.string.error_maximum_character)
		}
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
			// TODO: memory .....
			if (isNotEmpty()) {
				delete(length - 1, length).setExpressionBuilder()
			}
		}
	}

	fun equalClicked(saveHistory: (history: History) -> Unit) {
		if (outputMutableLiveData.value.isNullOrEmpty() ||
			"(???????+".contains(expressionBuilder.value!!.toString().last())
		) {
			showToastMessage(R.string.error_formula_is_wrong)
			return
		}
		if (isOverLimit) {
			showToastMessage(R.string.error_over_the_limit)
			return
		}

		if (currentState == CalculateState.INPUT) {
			setDisplayState(CalculateState.EVALUATE)
			onEvaluate(saveHistory)
		}

	}

	private fun onEvaluate(saveHistory: (history: History) -> Unit) {
		var currentInput = expressionBuilder.value!!.toString()
		val output = outputMutableLiveData.value!!.removeComma()
		if (currentInput.isNotEmpty()) {
			Timber.i("currentInput : $currentInput")
			if ("???????+".contains(currentInput.last())) {
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
			if ("(???????+".contains(currentInput.last())) {
				return
			}
			val result = Symbols().eval(
				expressionBuilder.value!!.toExpression().maybeAppendClosedBrackets()
			)
			if (result.isNaN() || result.isInfinite()) {
				outputMutableLiveData.value = ""
			} else {
				isOverLimit = result.isOverLimit()
				if (isOverLimit) {
					outputMutableLiveData.value = appStr(R.string.error_over_the_limit)
				} else {
					outputMutableLiveData.value = result.toSimpleString().makeCommaExpr()
				}
			}
		} else {
			outputMutableLiveData.value = ""
		}
	}


	private fun appendParens() {
		if (isMoreThanLeftParen() && checkLastOperatorAndLeftParen().not()) {
			appendExpression(")")
		} else if (expressionBuilder.value!!.isEdited && checkLastOperatorAndLeftParen().not()) {
			appendExpression("??(")
		} else {
			appendExpression("(")
		}
	}

	private fun isMoreThanLeftParen() = expressionBuilder.value!!.run {
		return@run getLeftParenCount() - getRightParenCount() > 0
	}

	private fun getLastInput() = expressionBuilder.value!!.lastOrNull() ?: CHAR_OF_EMPTY_EXPRESSION
	private fun checkLastOperatorAndLeftParen() = "s(???????+".contains(getLastInput())
	private fun isLastInputRightParen() = ")".contains(getLastInput())


	private fun appendDouble0() {
		if (isFirstCharAtOperandZero()) {
			Timber.i("First char at operand is Zero")
		} else if (isFirstOperandOrEmpty() || expressionBuilder.value!!.checkDouble0OperandMaxLength()) {
			appendExpression("0")
		} else {
			appendExpression("00")
		}
	}

	private fun isFirstCharAtOperandZero() = if (expressionBuilder.value!!.length >= 2) {
		"???????+".contains(expressionBuilder.value!!.run { this[length - 2] }) && getLastInput() == '0'
	} else {
		expressionBuilder.value!!.singleOrNull() == '0'
	}

	private fun isFirstOperandOrEmpty() = "s???????+".contains(getLastInput())


	private fun isStateResult() = currentState == CalculateState.RESULT
	private fun isStateNotResult() = isStateResult().not()


	private fun isEdited() = currentState == CalculateState.INPUT || currentState == CalculateState.ERROR

	fun setInputState() {
		currentState = CalculateState.INPUT
	}


	fun saveBuffer(index: Int) {
		if (memory[index].value.isNullOrEmpty()) {    // ????????? ???????????? ?????? - ??????
			if (expressionBuilder.value!!.isOperatorNotInExpr()) {    // ???????????? ???????????? ?????? ????????? ????????? ??????
				if (expressionBuilder.value.isNullOrEmpty()) {
					showToastMessage(R.string.error_nothing_to_save)
				} else if (expressionBuilder.value!!.isError || currentState == CalculateState.ERROR) {
					showToastMessage(R.string.error_cannot_save)
				} else {    // ?????? ??????
					viewModelScope.launch {
						context.writeString(
							key = "$BUFFER${index + 1}",
							value = expressionBuilder.value!!.toExpression()
						)
					}
				}
			} else {    // ????????? ?????? ??????
				showToastMessage(R.string.error_formula_cannot_save)
			}
		} else {    // ????????? ?????? ?????? ?????? - ??? ????????????
			var bufferedValue = memory[index].value!!
			if (bufferedValue.contains("-")) bufferedValue = "($bufferedValue)"
			appendBufferedValue(bufferedValue)
		}
	}

	private fun appendBufferedValue(bufferedValue: String) {
		if (checkLastOperatorAndLeftParen()) {
			if (expressionBuilder.value!!.checkExprMaxLength()) {
				appendExpression(bufferedValue)
			} else {
				showToastMessage(R.string.error_maximum_character)
			}
		} else {
			showToastMessage(R.string.error_operator_first)
		}
	}

	fun setDisplayState(state: CalculateState) {
		if (currentState != state) {
			currentState = state
			if (state == CalculateState.ERROR) {
				expressionBuilder.value!!.isError = true
			} else {
				// TODO: setColor
			}
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
		showToastMessage(R.string.message_all_memory_clear)
	}


	private fun appendExpression(input: String) {
		expressionBuilder.value!!.append(input).setExpressionBuilder()
	}

	fun setExpression(expr: String = "") {
		expressionBuilder.value = ExpressionBuilder(expr, isEdited())
	}


	private fun SpannableStringBuilder.setExpressionBuilder() {
		expressionBuilder.value = this as ExpressionBuilder
	}


	fun showToastMessage(@StringRes strResId: Int) {
		_toastMessage.value = appStr(strResId)
	}

}