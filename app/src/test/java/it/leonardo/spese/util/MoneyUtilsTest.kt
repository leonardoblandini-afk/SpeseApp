package it.leonardo.spese.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyUtilsTest {
    @Test
    fun parsesItalianDecimal() {
        assertEquals(12_345L, MoneyUtils.parseToCents("123,45"))
    }

    @Test
    fun parsesInternationalDecimal() {
        assertEquals(12_345L, MoneyUtils.parseToCents("123.45"))
    }

    @Test
    fun rejectsNegativeAndInvalidValues() {
        assertNull(MoneyUtils.parseToCents("-1"))
        assertNull(MoneyUtils.parseToCents("abc"))
    }
}
