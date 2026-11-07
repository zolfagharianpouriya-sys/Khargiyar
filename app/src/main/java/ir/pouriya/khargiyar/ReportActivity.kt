package ir.pouriya.khargiyar

import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import java.util.*

class ReportActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24,24,24,24)
            layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        }
        val title = TextView(this).apply {
            text = "گزارش هزینه‌ها"
            textSize = 28f
            gravity = 17
        }
        box.addView(title)

        val result = TextView(this).apply {
            textSize = 19f
            setPadding(0,30,0,0)
        }
        box.addView(result)

        val back = Button(this).apply { text = "بازگشت" }
        box.addView(back)
        back.setOnClickListener { finish() }

        setContentView(box)

        scope.launch {
            val dao = AppDb.get(this@ReportActivity).expenseDao()
            val now = System.currentTimeMillis()
            val day = 24L*60*60*1000
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
            }.timeInMillis
            val weekStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
            }.timeInMillis
            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH,1)
                set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
            }.timeInMillis
            val yearStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_YEAR,1)
                set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
            }.timeInMillis

            fun f(n: Long) = String.format(Locale("fa"), "%,d تومان", n)
            result.text = """
                امروز: ${f(dao.rangeTotal(todayStart, now))}

                این هفته: ${f(dao.rangeTotal(weekStart, now))}

                این ماه: ${f(dao.rangeTotal(monthStart, now))}

                امسال: ${f(dao.rangeTotal(yearStart, now))}
            """.trimIndent()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
