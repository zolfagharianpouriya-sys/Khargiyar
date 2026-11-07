package ir.pouriya.khargiyar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {
    @Insert suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    suspend fun all(): List<Expense>

    @Query("""
        SELECT COALESCE(SUM(amount),0) FROM expenses
        WHERE date(createdAt/1000,'unixepoch','localtime') =
              date('now','localtime')
    """)
    suspend fun todayTotal(): Long

    @Query("""
        SELECT COALESCE(SUM(amount),0) FROM expenses
        WHERE createdAt BETWEEN :from AND :to
    """)
    suspend fun rangeTotal(from: Long, to: Long): Long
}
