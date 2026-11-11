package it.leonardo.spese

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import it.leonardo.spese.data.Expense
import it.leonardo.spese.data.ExpenseDbHelper
import it.leonardo.spese.databinding.ActivityMainBinding
import it.leonardo.spese.databinding.DialogExpenseBinding
import it.leonardo.spese.ui.ExpenseAdapter
import it.leonardo.spese.util.ExpenseCalculator
import it.leonardo.spese.util.MoneyUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: ExpenseDbHelper
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        db = ExpenseDbHelper(this)
        adapter = ExpenseAdapter(
            onEdit = { showExpenseDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.expenseList.layoutManager = LinearLayoutManager(this)
        binding.expenseList.adapter = adapter
        binding.addExpense.setOnClickListener { showExpenseDialog() }
        refresh()
    }

    private fun refresh() {
        val expenses = db.getAll()
        adapter.submitList(expenses)
        binding.emptyView.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE

        val total = expenses.sumOf { it.amountCents }
        var dueManu = 0L
        var dueLeo = 0L
        expenses.forEach {
            val split = ExpenseCalculator.split(it.amountCents, it.manuPercentage)
            dueManu += split.first
            dueLeo += split.second
        }
        val paidManu = expenses.sumOf { it.paidManuCents }
        val paidLeo = expenses.sumOf { it.paidLeoCents }

        binding.summaryTotal.text = getString(R.string.summary_total, MoneyUtils.formatCents(total))
        binding.summaryManu.text = getString(
            R.string.summary_manu,
            MoneyUtils.formatCents(dueManu),
            MoneyUtils.formatCents(paidManu)
        )
        binding.summaryLeo.text = getString(
            R.string.summary_leo,
            MoneyUtils.formatCents(dueLeo),
            MoneyUtils.formatCents(paidLeo)
        )
    }

    private fun showExpenseDialog(existing: Expense? = null) {
        val form = DialogExpenseBinding.inflate(layoutInflater)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).apply { isLenient = false }
        val initialDate = existing?.date ?: isoFormat.format(Calendar.getInstance().time)

        form.inputDate.setText(initialDate)
        form.inputDescription.setText(existing?.description.orEmpty())
        existing?.let {
            form.inputAmount.setText(formatForInput(it.amountCents))
            form.inputPaidManu.setText(formatForInput(it.paidManuCents))
            form.inputPaidLeo.setText(formatForInput(it.paidLeoCents))
            form.radioSplit60.isChecked = it.manuPercentage == 60
            form.radioSplit50.isChecked = it.manuPercentage != 60
        } ?: run {
            form.radioSplit50.isChecked = true
            form.inputPaidManu.setText("0,00")
            form.inputPaidLeo.setText("0,00")
        }

        form.inputDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            try {
                calendar.time = requireNotNull(isoFormat.parse(form.inputDate.text.toString()))
            } catch (_: Exception) {
                // Mantiene la data corrente se il valore non è interpretabile.
            }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    form.inputDate.setText(isoFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) R.string.new_expense else R.string.edit_expense)
            .setView(form.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                form.layoutDescription.error = null
                form.layoutAmount.error = null

                val description = form.inputDescription.text?.toString()?.trim().orEmpty()
                val amount = MoneyUtils.parseToCents(form.inputAmount.text?.toString().orEmpty())
                val paidManu = MoneyUtils.parseToCents(form.inputPaidManu.text?.toString().orEmpty())
                val paidLeo = MoneyUtils.parseToCents(form.inputPaidLeo.text?.toString().orEmpty())

                var valid = true
                if (description.isBlank()) {
                    form.layoutDescription.error = getString(R.string.required_field)
                    valid = false
                }
                if (amount == null || amount <= 0) {
                    form.layoutAmount.error = getString(R.string.invalid_amount)
                    valid = false
                }
                if (paidManu == null) {
                    form.layoutPaidManu.error = getString(R.string.invalid_amount)
                    valid = false
                } else {
                    form.layoutPaidManu.error = null
                }
                if (paidLeo == null) {
                    form.layoutPaidLeo.error = getString(R.string.invalid_amount)
                    valid = false
                } else {
                    form.layoutPaidLeo.error = null
                }
                if (!valid) return@setOnClickListener

                val manuPercentage = if (form.radioSplit60.isChecked) 60 else 50
                val expense = Expense(
                    id = existing?.id ?: 0,
                    date = form.inputDate.text.toString(),
                    description = description,
                    amountCents = requireNotNull(amount),
                    manuPercentage = manuPercentage,
                    leoPercentage = 100 - manuPercentage,
                    paidManuCents = requireNotNull(paidManu),
                    paidLeoCents = requireNotNull(paidLeo)
                )
                if (existing == null) db.insert(expense) else db.update(expense)
                dialog.dismiss()
                refresh()
            }
        }
        dialog.show()
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_expense)
            .setMessage(getString(R.string.delete_message, expense.description))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                db.delete(expense.id)
                refresh()
            }
            .show()
    }

    private fun formatForInput(cents: Long): String =
        "%.2f".format(Locale.ITALY, cents / 100.0)

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}
