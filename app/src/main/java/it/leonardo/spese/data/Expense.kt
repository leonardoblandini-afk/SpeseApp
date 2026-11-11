package it.leonardo.spese.data

data class Expense(
    val id: Long = 0,
    val date: String,
    val description: String,
    val amountCents: Long,
    val manuPercentage: Int,
    val leoPercentage: Int,
    val paidManuCents: Long = 0,
    val paidLeoCents: Long = 0
)
