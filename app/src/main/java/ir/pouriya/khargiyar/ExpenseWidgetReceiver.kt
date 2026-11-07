package ir.pouriya.khargiyar

import android.appwidget.AppWidgetManager
import android.content.*
import android.widget.RemoteViews
import kotlinx.coroutines.*

class ExpenseWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        update(context)
    }

    companion object {
        fun update(context: Context) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val total = AppDb.get(context).expenseDao().todayTotal()
                    val manager = AppWidgetManager.getInstance(context)
                    val ids = manager.getAppWidgetIds(
                        ComponentName(context, ExpenseWidgetReceiver::class.java)
                    )
                    ids.forEach { id ->
                        val views = RemoteViews(context.packageName, R.layout.widget_expense)
                        views.setTextViewText(R.id.widgetTotal,
                            String.format(java.util.Locale("fa"), "%,d تومان", total))
                        manager.updateAppWidget(id, views)
                    }
                } finally { pending.finish() }
            }
        }
    }
}
