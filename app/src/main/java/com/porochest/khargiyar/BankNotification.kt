package com.porochest.khargiyar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object BankNotification {
    const val CHANNEL = "bank_expenses"
    fun show(context: Context, amount: Long, sms: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(CHANNEL, "برداشت بانکی", NotificationManager.IMPORTANCE_HIGH))
        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP; putExtra("bank_amount", amount); putExtra("bank_sms", sms) }
        val pi = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(context, CHANNEL) else @Suppress("DEPRECATION") android.app.Notification.Builder(context)
        manager.notify(731, n.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("خرج‌یار: برداشت بانکی جدید").setContentText("مبلغ ${amount} تومان؛ برای گفتن علت هزینه لمس کن").setContentIntent(pi).setAutoCancel(true).build())
    }
}