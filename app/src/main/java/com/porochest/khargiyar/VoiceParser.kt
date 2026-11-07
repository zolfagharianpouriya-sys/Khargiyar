package com.porochest.khargiyar

object VoiceParser {
    fun normalize(s: String): String = s.map { ch -> when(ch) { in '۰'..'۹' -> ('0'.code + ch.code - '۰'.code).toChar(); in '٠'..'٩' -> ('0'.code + ch.code - '٠'.code).toChar(); '٬','،' -> ','; else -> ch } }.joinToString("").trim()
    fun parse(text: String): Pair<Long,String>? {
        val s = normalize(text).replace("تومان", " ").replace("تومن", " ").replace("هزار", " هزار ").replace("میلیون", " میلیون ")
        val match = Regex("([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*(هزار|میلیون)?").find(s) ?: return null
        var n = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        when(match.groupValues[2]) { "هزار" -> n *= 1000; "میلیون" -> n *= 1_000_000 }
        if (n > 0 && n < 10_000 && !s.contains("ریال")) n *= 1000
        val title = (s.removeRange(match.range).replace("هزار", "").replace("میلیون", "").trim().ifBlank { "هزینه متفرقه" })
        return n.toLong() to title
    }
}