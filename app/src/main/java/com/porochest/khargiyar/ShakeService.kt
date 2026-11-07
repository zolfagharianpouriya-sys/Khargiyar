package com.porochest.khargiyar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import kotlin.math.sqrt

class ShakeService : Service(), SensorEventListener {
    private lateinit var sm: SensorManager
    private var lastShake = 0L
    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel("shake_service", "فعال‌سازی با تکان", NotificationManager.IMPORTANCE_LOW))
        val pi = PendingIntent.getActivity(this, 9, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "shake_service") else @Suppress("DEPRECATION") Notification.Builder(this)
        startForeground(91, notification.setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("خرج‌یار فعال است").setContentText("برای ثبت سریع هزینه گوشی را تکان بده").setContentIntent(pi).build())
        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }
    override fun onSensorChanged(e: SensorEvent) {
        val x=e.values[0]; val y=e.values[1]; val z=e.values[2]
        val g=sqrt(x*x+y*y+z*z)/SensorManager.GRAVITY_EARTH
        val now=System.currentTimeMillis()
        if (g > 2.5f && now-lastShake > 5000) {
            lastShake=now
            val intent=Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("start_voice", true)
            val pi=PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(92, Notification.Builder(this, "shake_service").setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("خرج‌یار آماده ثبت هزینه است").setContentText("برای گفتن مبلغ لمس کن").setContentIntent(pi).setAutoCancel(true).build())
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onDestroy() { sm.unregisterListener(this); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
