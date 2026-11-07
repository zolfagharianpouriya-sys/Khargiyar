package com.porochest.khargiyar

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var db: ExpenseDb
    private lateinit var status: TextView
    private lateinit var list: LinearLayout
    private var pendingBankAmount: Long? = null
    private val voiceRequest = 44
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db=ExpenseDb(this)
        requestPermissionsIfNeeded()
        buildUi()
        handleIntent(intent)
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); handleIntent(intent) }
    private fun requestPermissionsIfNeeded() {
        val perms= mutableListOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.RECEIVE_SMS)
        if (Build.VERSION.SDK_INT>=33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing=perms.filter { checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED }
        if(missing.isNotEmpty()) requestPermissions(missing.toTypedArray(),22)
    }
    private fun buildUi() {
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,32,28,24); setBackgroundColor(0xFFF7F8FA.toInt()) }
        fun text(t:String,size:Float,bold:Boolean=false)=TextView(this).apply { text=t; textSize=size; setTextColor(0xFF1C2925.toInt()); if(bold)setTypeface(null,1); setPadding(0,8,0,8) }
        root.addView(text("خرج‌یار",28f,true)); root.addView(text("دستیار ثبت هوشمند هزینه‌ها",15f))
        status=text("برای ثبت هزینه دکمه را بزن یا گوشی را تکان بده.",14f); root.addView(status)
        val speak=Button(this).apply { text="🎙 ثبت هزینه با صدا"; setOnClickListener { launchVoice() } }; root.addView(speak)
        val service=Button(this).apply { text="فعال‌سازی تشخیص تکان گوشی"; setOnClickListener { val i=Intent(this@MainActivity,ShakeService::class.java); if(Build.VERSION.SDK_INT>=26) startForegroundService(i) else startService(i); status.text="سرویس تکان فعال شد؛ اعلان دائمی نمایش داده می‌شود." } }; root.addView(service)
        root.addView(text("گزارش هزینه‌ها",20f,true))
        val reports=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        listOf("امروز" to "day","هفته" to "week","ماه" to "month","سال" to "year").forEach { (label,key) -> reports.addView(Button(this).apply { text=label; setOnClickListener { val start=if(key=="day") ExpenseDb.startOf("day") else ExpenseDb.startOf(key); status.text="$label: ${db.sumSince(start).toString().reversed().chunked(3).joinToString(",").reversed()} تومان" } }, LinearLayout.LayoutParams(0,-2,1f)) }
        root.addView(reports)
        root.addView(text("آخرین هزینه‌ها",20f,true)); list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }; root.addView(list)
        setContentView(ScrollView(this).apply { addView(root) })
        refreshList()
    }
    private fun handleIntent(i:Intent?) {
        if(i==null)return
        val amount=i.getLongExtra("bank_amount",-1L)
        if(amount>0) { pendingBankAmount=amount; status.text="برداشت ${amount} تومان ثبت شده؛ بگو بابت چه بود."; launchVoice() }
        if(i.getBooleanExtra("start_voice",false)) launchVoice()
        i.removeExtra("bank_amount"); i.removeExtra("start_voice")
    }
    private fun launchVoice() {
        try {
            val i=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fa-IR"); putExtra(RecognizerIntent.EXTRA_PROMPT, if(pendingBankAmount!=null) "این برداشت بابت چه بود؟" else "مثلاً بگو ۱۲۰ هزار تومان بنزین")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1)
            }
            startActivityForResult(i,voiceRequest)
        } catch(e:Exception) { status.text="سرویس تشخیص گفتار فارسی روی گوشی در دسترس نیست. می‌توانی دستی ثبت کنی."; manualEntry() }
    }
    @Deprecated("Deprecated in Android API but broadly supported")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=voiceRequest || resultCode!=RESULT_OK)return
        val spoken=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        if(spoken.isBlank()) { status.text="صدایی تشخیص داده نشد."; return }
        val bank=pendingBankAmount
        if(bank!=null) {
            db.add(bank,spoken,"bank_sms"); pendingBankAmount=null; status.text="ثبت شد: $spoken — ${bank} تومان"
        } else {
            val parsed=VoiceParser.parse(spoken)
            if(parsed==null) { status.text="مبلغ را متوجه نشدم؛ دوباره بگو یا دستی ثبت کن."; return }
            db.add(parsed.first,parsed.second,"voice"); status.text="ثبت شد: ${parsed.second} — ${parsed.first} تومان"
        }
        refreshList()
    }
    private fun manualEntry() {
        val amount=EditText(this).apply { hint="مبلغ به تومان"; inputType=2 }
        val title=EditText(this).apply { hint="شرح هزینه" }
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; addView(amount); addView(title) }
        android.app.AlertDialog.Builder(this).setTitle("ثبت دستی هزینه").setView(box).setPositiveButton("ذخیره") { _,_-> val a=amount.text.toString().toLongOrNull(); if(a!=null && a>0){db.add(a,title.text.toString().ifBlank{"هزینه متفرقه"});refreshList()} }.setNegativeButton("لغو",null).show()
    }
    private fun refreshList() {
        if(!::list.isInitialized)return
        list.removeAllViews()
        db.all().take(12).forEach { e ->
            val row=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,8,0,8) }
            row.addView(TextView(this).apply { text="${e.title}  |  ${String.format(Locale.US,"%,d",e.amount)} تومان"; textSize=15f; setTextColor(0xFF26332E.toInt()) })
            row.addView(TextView(this).apply { text=ExpenseDb.date(e.created); textSize=11f; setTextColor(0xFF65736D.toInt()) })
            list.addView(row)
        }
    }
}