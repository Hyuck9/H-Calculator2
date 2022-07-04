package me.hyuck9.calculator.extensions

import org.junit.Test

import org.junit.Assert.*

internal class StringExtensionsTest {

	@Test
	fun containsWords() {
		val str = "동해물과 백두산이 마르고 닳도록"
		assertTrue(str.containsWords("동해"))
		assertTrue(str.containsWords("가나", "다라", "백두산"))
		assertFalse(str.containsWords("가나다라마"))
	}
}