package me.hyuck9.calculator.view.viewmodel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.hyuck9.calculator.extensions.toExpression
import me.hyuck9.calculator.extensions.toSimpleString
import org.mariuszgromada.math.mxparser.Expression
import timber.log.Timber

class CalculatorInputViewModel : ViewModel() {

	private val operators = listOf('^', '÷', '×', '+', '−')
	private val numRegex = "-?[0-9]+\\.?[0-9]*".toRegex()
	private var isDecimal = false

	private val inputMutableLiveData = MutableLiveData("")
	val inputLiveData: LiveData<String> = inputMutableLiveData

	private val outputMutableLiveData = MutableLiveData("")
	val outputLiveData: LiveData<String> = outputMutableLiveData


	fun numberClicked(num: String) {
		Timber.d("num : $num")
		insert(num)
	}

	fun operatorClicked(oper: String) {
		insert(oper)
	}

	fun clearAll(): Boolean {
		inputMutableLiveData.value = ""
		outputMutableLiveData.value = ""
		return true
	}

	fun backspaceClicked() {
		val expression = inputLiveData.value ?: ""
		inputMutableLiveData.value = if (expression.isEmpty()) "" else expression.substring(0, expression.length - 1)
	}



	fun calculateOutput() {
		val currentInput = inputLiveData.value!!
		val result: Double
		if (currentInput.matches(numRegex)) {
			outputMutableLiveData.value = ""
		} else if (currentInput.isNotEmpty()) {
			result = Expression(inputLiveData.value!!.toExpression()).calculate()
			if (result.toString() != "NaN") {
				outputMutableLiveData.value = result.toSimpleString()
			}
		} else {
			outputMutableLiveData.value = ""
		}
	}


	private fun insert(input: String) {
		val expression = inputLiveData.value ?: ""
		if (input.isDigitsOnly().not() && input != ".") {
			isDecimal = false
		}
		if (expression.isEmpty()) {	// input이 비어있는 경우 (첫번째 입력)
			add(input)
		} else {
			val lastChar = expression.last()
			if (input.isDigitsOnly()) {
				add(input)
			} else if (operators.contains(input[0])) {
				if (operators.contains(lastChar)) {
					if (expression.length > 2) {
						if (operators.contains(expression[expression.length - 2]) && lastChar == '−') {
							if ("÷×+".contains(input[0])) {
								replaceTwoChar(input)
							} else {
								return
							}
						}
					}
					if (input == "−" && "×÷".contains(lastChar)) {
						add(input)
					} else {
						replace(input)
					}
				} else {
					add(input)
				}
			} else {
				add(input)
			}
		}
	}

	private fun add(input: String) {
		val expression = inputLiveData.value ?: ""
		inputMutableLiveData.value = expression + input
		Timber.i("inputLiveData : ${inputMutableLiveData.value}")
	}

	private fun replace(input: String) {
		val expression = inputLiveData.value ?: ""
		inputMutableLiveData.value = expression.substring(0, expression.length - 1) + input
	}

	private fun replaceTwoChar(input: String) {
		val expression = inputLiveData.value ?: ""
		inputMutableLiveData.value = expression.substring(0, expression.length - 2) + input
	}

}