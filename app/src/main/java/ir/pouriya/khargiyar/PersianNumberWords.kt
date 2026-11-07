package ir.pouriya.khargiyar

object PersianNumberWords {
    private val ones = mapOf(
        "صفر" to 0, "یک" to 1, "دو" to 2, "سه" to 3, "چهار" to 4, "پنج" to 5,
        "شش" to 6, "شیش" to 6, "هفت" to 7, "هشت" to 8, "نه" to 9, "ده" to 10,
        "یازده" to 11, "دوازده" to 12, "سیزده" to 13, "چهارده" to 14,
        "پانزده" to 15, "شانزده" to 16, "هفده" to 17, "هجده" to 18, "نوزده" to 19
    )
    private val tens = mapOf(
        "بیست" to 20, "سی" to 30, "چهل" to 40, "پنجاه" to 50,
        "شصت" to 60, "هفتاد" to 70, "هشتاد" to 80, "نود" to 90
    )
    private val hundreds = mapOf(
        "صد" to 100, "دویست" to 200, "سیصد" to 300, "چهارصد" to 400,
        "پانصد" to 500, "ششصد" to 600, "هفتصد" to 700, "هشتصد" to 800, "نهصد" to 900
    )

    fun parse(text: String): Long? {
        val s = text.replace("‌"," ").replace("  "," ").trim()
        val tokens = s.split(" ").filter { it.isNotBlank() }
        var total = 0L
        var current = 0L
        var found = false

        for (t0 in tokens) {
            val t = t0.trim()
            when {
                ones.containsKey(t) -> { current += ones[t]!!; found = true }
                tens.containsKey(t) -> { current += tens[t]!!; found = true }
                hundreds.containsKey(t) -> { current += hundreds[t]!!; found = true }
                t == "هزار" -> { current = if (current == 0L) 1 else current; total += current * 1000; current = 0; found = true }
                t == "میلیون" -> { current = if (current == 0L) 1 else current; total += current * 1_000_000; current = 0; found = true }
                t == "میلیارد" -> { current = if (current == 0L) 1 else current; total += current * 1_000_000_000; current = 0; found = true }
            }
        }
        return if (found) total + current else null
    }
}
