package it.leonardo.spese.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseCalculatorTest {
    @Test
    fun split50_50() {
        assertEquals(5_000L to 5_000L, ExpenseCalculator.split(10_000L, 50))
    }

    @Test
    fun split60Manu40Leo() {
        assertEquals(6_000L to 4_000L, ExpenseCalculator.split(10_000L, 60))
    }

    @Test
    fun splitKeepsTotalAfterRounding() {
        val result = ExpenseCalculator.split(1L, 50)
        assertEquals(1L, result.first + result.second)
    }
}
