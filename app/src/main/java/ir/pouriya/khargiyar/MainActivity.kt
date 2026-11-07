package ir.pouriya.khargiyar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var total: TextView
    private lateinit var status: TextView
    private lateinit var adapter: ExpenseAdapter

    private val permissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { loadData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        total = findViewById(R.id.todayTotal)
        status = findViewById(R.id.statusText)

        val list = findViewById<RecyclerView>(R.id.list)
        adapter = ExpenseAdapter()
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<Button>(R.id.startButton).setOnClickListener { startListeningService() }
        findViewById<Button>(R.id.micButton).setOnClickListener { startListeningService() }
        findViewById<Button>(R.id.reportButton).setOnClickListener { startActivity(Intent(this, ReportActivity::class.java)) }

        requestNeededPermissions()
        loadData()
    }

    private fun requestNeededPermissions() {
        val p = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (android.os.Build.VERSION.SDK_INT >= 33) p += Manifest.permission.POST_NOTIFICATIONS
        permissions.launch(p.toTypedArray())
    }

    private fun startListeningService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestNeededPermissions()
            return
        }
        ContextCompat.startForegroundService(this, Intent(this, ShakeVoiceService::class.java))
        status.text = "وضعیت: فعال — گوشی را تکان بده"
    }

    private fun loadData() {
        scope.launch {
            val dao = AppDb.get(this@MainActivity).expenseDao()
            total.text = "هزینه‌های امروز: ${format(dao.todayTotal())} تومان"
            adapter.submit(dao.all())
        }
    }

    private fun format(n: Long): String =
        String.format(java.util.Locale("fa"), "%,d", n)
}
