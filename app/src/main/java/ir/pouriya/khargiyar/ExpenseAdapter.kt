package ir.pouriya.khargiyar

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.VH>() {
    private var items = listOf<Expense>()

    fun submit(x: List<Expense>) { items = x; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = TextView(parent.context).apply {
            textSize = 17f
            setPadding(8, 18, 8, 18)
            layoutParams = ViewGroup.LayoutParams(-1, -2)
        }
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        val date = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault()).format(Date(e.createdAt))
        holder.v.text = "$date\n${e.title} — ${String.format(Locale("fa"), "%,d", e.amount)} تومان"
    }

    override fun getItemCount() = items.size
    class VH(val v: TextView) : RecyclerView.ViewHolder(v)
}
