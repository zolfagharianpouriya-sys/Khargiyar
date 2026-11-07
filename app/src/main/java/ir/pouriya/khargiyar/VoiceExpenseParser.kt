package com.porochest.khargiyar

object VoiceParser {
    // یک پارسر ساده برای استخراج مبلغ و عنوان از متن گفتار
    fun parse(text: String): Pair<Long, String>? {
        // پیدا کردن اعداد در متن (با پشتیبانی از اعداد فارسی)
        val normalized = text.replace("۰","0").replace("۱","1").replace("۲","2")
            .replace("۳","3").replace("۴","4").replace("۵","5")
            .replace("۶","6").replace("۷","7").replace("۸","8").replace("۹","9")
        
        val numberRegex = Regex("\\d+")
        val match = numberRegex.find(normalized) ?: return null
        var amount = match.value.toLongOrNull() ?: return null

        // بررسی کلمات هزار، میلیون و تومان
        if (normalized.contains("هزار") || normalized.contains("تومن") || normalized.contains("تومان")) {
            if (normalized.contains("هزار")) amount *= 1000
        }
        
        // حذف عدد و کلمات کلیدی از متن برای پیدا کردن عنوان
        val title = normalized.replace(match.value, "")
            .replace("هزار", "").replace("تومن", "").replace("تومان", "")
            .trim()
            .ifBlank { "هزینه" }

        return Pair(amount, title)
    }
}
