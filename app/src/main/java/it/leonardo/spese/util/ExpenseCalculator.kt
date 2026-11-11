package it.leonardo.spese.util

object ExpenseCalculator {
    fun split(amountCents: Long, manuPercentage: Int): Pair<Long, Long> {
        require(amountCents >= 0) { "L'importo non può essere negativo" }
        require(manuPercentage in 0..100) { "Percentuale non valida" }
        val dueManu = (amountCents * manuPercentage + 50L) / 100L
        return dueManu to (amountCents - dueManu)
    }
}
