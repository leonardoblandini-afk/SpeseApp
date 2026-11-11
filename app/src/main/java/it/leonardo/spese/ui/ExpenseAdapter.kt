package it.leonardo.spese.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import it.leonardo.spese.R
import it.leonardo.spese.data.Expense
import it.leonardo.spese.databinding.ItemExpenseBinding
import it.leonardo.spese.util.ExpenseCalculator
import it.leonardo.spese.util.MoneyUtils
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) = with(binding) {
            val context = root.context
            val (dueManu, dueLeo) = ExpenseCalculator.split(
                expense.amountCents,
                expense.manuPercentage
            )
            textDescription.text = expense.description
            textAmount.text = MoneyUtils.formatCents(expense.amountCents)
            textMeta.text = context.getString(
                R.string.expense_meta,
                formatDate(expense.date),
                context.getString(
                    if (expense.manuPercentage == 60) R.string.split_40_60 else R.string.split_50
                )
            )
            textDue.text = context.getString(
                R.string.due_values,
                MoneyUtils.formatCents(dueManu),
                MoneyUtils.formatCents(dueLeo)
            )
            textPaid.text = context.getString(
                R.string.paid_values,
                MoneyUtils.formatCents(expense.paidManuCents),
                MoneyUtils.formatCents(expense.paidLeoCents)
            )
            buttonEdit.setOnClickListener { onEdit(expense) }
            buttonDelete.setOnClickListener { onDelete(expense) }
        }
    }

    private fun formatDate(isoDate: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).apply { isLenient = false }
        val output = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        output.format(requireNotNull(input.parse(isoDate)))
    } catch (_: Exception) {
        isoDate
    }

    private object DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem == newItem
    }
}
