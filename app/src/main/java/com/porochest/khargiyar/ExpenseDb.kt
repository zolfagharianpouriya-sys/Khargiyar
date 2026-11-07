package com.porochest.khargiyar

import android.content.Context
import androidx.room.*
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val title: String,
    val source: String = "manual",
    val created: Long = System.currentTimeMillis()
)

@Dao
interface ExpenseDao {
    @Insert
    fun insert(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY created DESC")
    fun all(): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE created >= :since")
    fun sumSince(since: Long): Long
}

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class ExpenseDb : RoomDatabase() {
    abstract fun dao(): ExpenseDao

    // متدهای کمکی برای دسترسی سریع در MainActivity
    fun add(amount: Long, title: String, source: String = "manual") {
        dao().insert(Expense(amount = amount, title = title, source = source))
    }

    fun all(): List<Expense> = dao().all()
    fun sumSince(since: Long): Long = dao().sumSince(since)

    companion object {
        @Volatile private var INSTANCE: ExpenseDb? = null
        fun get(context: Context): ExpenseDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDb::class.java,
                    "khargiyar.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        // متدهای استاتیک برای محاسبه زمان
        fun startOf(period: String): Long {
            val cal = Calendar.getInstance()
            when (period) {
                "day" -> cal.set(Calendar.HOUR_OF_DAY, 0)
                "week" -> cal.add(Calendar.DAY_OF_YEAR, -7)
                "month" -> cal.add(Calendar.MONTH, -1)
                "year" -> cal.add(Calendar.YEAR, -1)
            }
            if (period == "day") {
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun date(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
