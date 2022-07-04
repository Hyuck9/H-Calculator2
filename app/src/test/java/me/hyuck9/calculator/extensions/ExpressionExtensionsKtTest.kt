package me.hyuck9.calculator.extensions

import org.junit.Assert.assertEquals
import org.junit.Test


internal class ExpressionExtensionsKtTest {


	@Test
	fun addComma() {
		val number1 = "1234.5678900001"
		val number2 = "1,234.5678"
		val number3 = "1,234"
		val number4 = "123456789012345"
		val number5 = "123456789012345678901"

		println(number1.addComma())
		println(number2.addComma())
		println(number3.addComma())
		println(number4.addComma())
		println(number5.addComma())

		assertEquals(number1.addComma(), "1,234.5678900001")
		assertEquals(number2.addComma(), "1,234.5678")
		assertEquals(number3.addComma(), "1,234")
		assertEquals(number4.addComma(), "123,456,789,012,345")
		assertEquals(number5.addComma(), "123,456,789,012,345,678,901")
	}

}