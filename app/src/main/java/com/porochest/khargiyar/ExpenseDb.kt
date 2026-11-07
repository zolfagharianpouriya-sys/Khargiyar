package com.porochest.khargiyar

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Expense(val id: Long, val amount: Long, val title: String, val source: String, val created: Long)

class ExpenseDb(ctx: Context) : SQLiteOpenHelper(ctx, "khargiyar.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE expenses(id INTEGER PRIMARY KEY AUTOINCREMENT, amount INTEGER NOT NULL, title TEXT NOT NULL, source TEXT NOT NULL, created INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(amount: Long, title: String, source: String = "voice") {
        writableDatabase.insert("expenses", null, ContentValues().apply {
            put("amount", amount); put("title", title); put("source", source); put("created", System.currentTimeMillis())
        })
    }
    fun all(): List<Expense> {
        val out = mutableListOf<Expense>()
        readableDatabase.rawQuery("SELECT id,amount,title,source,created FROM expenses ORDER BY created DESC", null).use { c ->
            while (c.moveToNext()) out.add(Expense(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getLong(4)))
        }
        return out
    }
    fun sumSince(start: Long): Long = readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM expenses WHERE created>=?", arrayOf(start.toString())).use { c -> if(c.moveToFirst()) c.getLong(0) else 0L }
    fun delete(id: Long) { writableDatabase.delete("expenses", "id=?", arrayOf(id.toString())) }
    companion object {
        fun startOf(period: String): Long {
            val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }
            when(period) { "week" -> { cal.firstDayOfWeek = Calendar.SATURDAY; cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) }; "month" -> cal.set(Calendar.DAY_OF_MONTH,1); "year" -> cal.set(Calendar.DAY_OF_YEAR,1) }
            return cal.timeInMillis
        }
        fun date(ts: Long): String = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale("fa","IR")).format(Date(ts))
    }
}
