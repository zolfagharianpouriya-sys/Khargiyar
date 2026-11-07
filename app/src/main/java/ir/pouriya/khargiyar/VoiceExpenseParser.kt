package ir.pouriya.khargiyar

import java.util.Locale

data class ParsedExpense(val amount: Long, val title: String)

object VoiceExpenseParser {
    private val digitAmount = Regex("""(\d+(?:\.\d+)?)\s*(میلیون|هزار|تومن|تومان)?""")

    fun parse(raw: String): ParsedExpense? {
        val normalized = PersianNumbers.normalize(raw.lowercase(Locale("fa", "IR")))
            .replace("تومن", "تومان")
            .replace(Regex("\\s+"), " ")
            .trim()

        val numericMatches = digitAmount.findAll(normalized).toList()
        val numeric = numericMatches.maxByOrNull { it.range.last - it.range.first }

        val parsed: Pair<Long, IntRange>? = if (numeric != null) {
            val n = numeric.groupValues[1].toDoubleOrNull() ?: return null
            val unit = numeric.groupValues[2]
            val amount = when {
                unit.contains("میلیون") -> (n * 1_000_000).toLong()
                unit.contains("هزار") -> (n * 1_000).toLong()
                else -> n.toLong()
            }
            amount to numeric.range
        } else {
            val wordsAmount = PersianNumberWords.parse(normalized) ?: return null
            val hit = Regex("(صفر|یک|دو|سه|چهار|پنج|شش|شیش|هفت|هشت|نه|ده|یازده|دوازده|سیزده|چهارده|پانزده|شانزده|هفده|هجده|نوزده|بیست|سی|چهل|پنجاه|شصت|هفتاد|هشتاد|نود|صد|دویست|سیصد|چهارصد|پانصد|ششصد|هفتصد|هشتصد|نهصد)(\\s+(و\\s+)?(صفر|یک|دو|سه|چهار|پنج|شش|هفت|هشت|نه|ده|یازده|دوازده|سیزده|چهارده|پانزده|شانزده|هفده|هجده|نوزده|بیست|سی|چهل|پنجاه|شصت|هفتاد|هشتاد|نود|صد|دویست|سیصد|چهارصد|پانصد|ششصد|هفتصد|هشتصد|نهصد|هزار|میلیون|میلیارد))*").find(normalized)
            wordsAmount to (hit?.range ?: 0..normalized.length)
        }

        var title = (normalized.substring(0, parsed.second.first) + " " +
                normalized.substring((parsed.second.last + 1).coerceAtMost(normalized.length)))
            .replace("تومان", "")
            .replace("هزار", "")
            .replace("میلیون", "")
            .replace("میلیارد", "")
            .replace(Regex("^(پول|هزینه|خرید|برای)\\s+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (title.isBlank()) title = "هزینه"
        return ParsedExpense(parsed.first, title)
    }
}
