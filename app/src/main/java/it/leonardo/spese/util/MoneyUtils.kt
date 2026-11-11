package it.leonardo.spese.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object MoneyUtils {
    fun parseToCents(input: String): Long? {
        var value = input.trim()
            .replace("€", "")
            .replace(" ", "")
            .replace("\u00A0", "")
        if (value.isBlank()) return null

        val comma = value.lastIndexOf(',')
        val dot = value.lastIndexOf('.')
        value = when {
            comma >= 0 && dot >= 0 && comma > dot -> value.replace(".", "").replace(',', '.')
            comma >= 0 && dot >= 0 -> value.replace(",", "")
            comma >= 0 -> value.replace(',', '.')
            else -> value
        }

        return try {
            val decimal = BigDecimal(value).setScale(2, RoundingMode.HALF_UP)
            if (decimal.signum() < 0) null else decimal.movePointRight(2).longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    fun formatCents(cents: Long): String = NumberFormat
        .getCurrencyInstance(Locale.ITALY)
        .format(BigDecimal.valueOf(cents, 2))
}
