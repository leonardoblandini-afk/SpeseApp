package it.leonardo.spese.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DATE TEXT NOT NULL,
                $COL_DESCRIPTION TEXT NOT NULL,
                $COL_AMOUNT_CENTS INTEGER NOT NULL CHECK($COL_AMOUNT_CENTS >= 0),
                $COL_MANU_PERCENTAGE INTEGER NOT NULL CHECK($COL_MANU_PERCENTAGE IN (50, 60)),
                $COL_LEO_PERCENTAGE INTEGER NOT NULL CHECK($COL_LEO_PERCENTAGE IN (50, 40)),
                $COL_PAID_MANU_CENTS INTEGER NOT NULL DEFAULT 0 CHECK($COL_PAID_MANU_CENTS >= 0),
                $COL_PAID_LEO_CENTS INTEGER NOT NULL DEFAULT 0 CHECK($COL_PAID_LEO_CENTS >= 0)
            )
            """.trimIndent()
        )

        val seed = ContentValues().apply {
            put(COL_DATE, "2026-11-01")
            put(COL_DESCRIPTION, "prova")
            put(COL_AMOUNT_CENTS, 10_000L)
            put(COL_MANU_PERCENTAGE, 60)
            put(COL_LEO_PERCENTAGE, 40)
            put(COL_PAID_MANU_CENTS, 0L)
            put(COL_PAID_LEO_CENTS, 0L)
        }
        db.insertOrThrow(TABLE_EXPENSES, null, seed)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Prima versione dello schema. Le migrazioni future andranno aggiunte qui.
    }

    fun getAll(): List<Expense> {
        val expenses = mutableListOf<Expense>()
        readableDatabase.query(
            TABLE_EXPENSES,
            null,
            null,
            null,
            null,
            null,
            "$COL_DATE DESC, $COL_ID DESC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val dateIndex = cursor.getColumnIndexOrThrow(COL_DATE)
            val descriptionIndex = cursor.getColumnIndexOrThrow(COL_DESCRIPTION)
            val amountIndex = cursor.getColumnIndexOrThrow(COL_AMOUNT_CENTS)
            val manuPercentageIndex = cursor.getColumnIndexOrThrow(COL_MANU_PERCENTAGE)
            val leoPercentageIndex = cursor.getColumnIndexOrThrow(COL_LEO_PERCENTAGE)
            val paidManuIndex = cursor.getColumnIndexOrThrow(COL_PAID_MANU_CENTS)
            val paidLeoIndex = cursor.getColumnIndexOrThrow(COL_PAID_LEO_CENTS)

            while (cursor.moveToNext()) {
                expenses += Expense(
                    id = cursor.getLong(idIndex),
                    date = cursor.getString(dateIndex),
                    description = cursor.getString(descriptionIndex),
                    amountCents = cursor.getLong(amountIndex),
                    manuPercentage = cursor.getInt(manuPercentageIndex),
                    leoPercentage = cursor.getInt(leoPercentageIndex),
                    paidManuCents = cursor.getLong(paidManuIndex),
                    paidLeoCents = cursor.getLong(paidLeoIndex)
                )
            }
        }
        return expenses
    }

    fun insert(expense: Expense): Long = writableDatabase.insertOrThrow(
        TABLE_EXPENSES,
        null,
        expense.toValues()
    )

    fun update(expense: Expense): Int = writableDatabase.update(
        TABLE_EXPENSES,
        expense.toValues(),
        "$COL_ID = ?",
        arrayOf(expense.id.toString())
    )

    fun delete(id: Long): Int = writableDatabase.delete(
        TABLE_EXPENSES,
        "$COL_ID = ?",
        arrayOf(id.toString())
    )

    private fun Expense.toValues() = ContentValues().apply {
        put(COL_DATE, date)
        put(COL_DESCRIPTION, description)
        put(COL_AMOUNT_CENTS, amountCents)
        put(COL_MANU_PERCENTAGE, manuPercentage)
        put(COL_LEO_PERCENTAGE, leoPercentage)
        put(COL_PAID_MANU_CENTS, paidManuCents)
        put(COL_PAID_LEO_CENTS, paidLeoCents)
    }

    companion object {
        private const val DATABASE_NAME = "spese.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_EXPENSES = "expenses"
        private const val COL_ID = "id"
        private const val COL_DATE = "expense_date"
        private const val COL_DESCRIPTION = "description"
        private const val COL_AMOUNT_CENTS = "amount_cents"
        private const val COL_MANU_PERCENTAGE = "manu_percentage"
        private const val COL_LEO_PERCENTAGE = "leo_percentage"
        private const val COL_PAID_MANU_CENTS = "paid_manu_cents"
        private const val COL_PAID_LEO_CENTS = "paid_leo_cents"
    }
}
