package com.porochest.khargiyar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class BankSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString(" ") { it.messageBody.orEmpty() }
        val normalized = VoiceParser.normalize(body).replace("٬", ",").replace("،", ",")
        val lower = normalized.lowercase()
        val debitSignals = listOf("برداشت", "خرید", "پرداخت", "انتقال از", "برداشت از")
        if (debitSignals.none { lower.contains(it) }) return
        if (listOf("رمز پویا", "رمز یکبار", "رمز دوم", "واریز", "مانده:", "موجودی:").any { lower.contains(it) }) return
        val amountText = Regex("(?:مبلغ(?:\\s+تراکنش)?|به مبلغ)\\s*[:：]?\\s*([0-9۰-۹٠-٩][0-9۰-۹٠-٩,٬]*)").find(normalized)?.groupValues?.get(1)
            ?: Regex("(?:برداشت|خرید|پرداخت)\\D{0,18}([0-9۰-۹٠-٩][0-9۰-۹٠-٩,٬]*)").find(normalized)?.groupValues?.get(1) ?: return
        val raw = VoiceParser.normalize(amountText).replace(",", "").toLongOrNull() ?: return
        if (raw <= 0L) return
        val toman = if (lower.contains("ریال")) raw / 10 else raw
        if (toman <= 0L) return
        BankNotification.show(context, toman, body)
    }
}