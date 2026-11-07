package ir.pouriya.khargiyar

import android.app.*
import android.content.*
import android.hardware.*
import android.media.AudioManager
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.Locale
import kotlin.math.sqrt

class ShakeVoiceService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lastShake = 0L
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(10, notification("منتظر تکان دادن گوشی..."))
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        tts = TextToSpeech(this) { tts?.language = Locale("fa", "IR") }
    }

    override fun onSensorChanged(e: SensorEvent) {
        val g = sqrt((e.values[0]*e.values[0] + e.values[1]*e.values[1] + e.values[2]*e.values[2]).toDouble())
        val now = System.currentTimeMillis()
        if (g > 15 && now - lastShake > 2500) {
            lastShake = now
            beepAndListen()
        }
    }

    private fun beepAndListen() {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
        speakStatus("بفرمایید")
        Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 450)
    }

    private fun speakStatus(s: String) {
        tts?.speak(s, TextToSpeech.QUEUE_FLUSH, null, "status")
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speakStatus("تشخیص صدا در این گوشی در دسترس نیست")
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
                val parsed = VoiceExpenseParser.parse(text)
                if (parsed != null) {
                    scope.launch(Dispatchers.IO) {
                        AppDb.get(this@ShakeVoiceService).expenseDao()
                            .insert(Expense(amount = parsed.amount, title = parsed.title))
                        ExpenseWidgetReceiver.update(this@ShakeVoiceService)
                    }
                    speakStatus("ثبت شد")
                    updateNotification("ثبت شد: ${parsed.title} - ${parsed.amount} تومان")
                } else {
                    speakStatus("مبلغ را متوجه نشدم")
                }
            }
            override fun onError(error: Int) {
                updateNotification("صدای واضح دریافت نشد؛ دوباره تکان دهید")
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer?.startListening(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun createChannel() {
        val c = NotificationChannel("expense_service", "ثبت هزینه", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(c)
    }

    private fun notification(text: String): Notification =
        NotificationCompat.Builder(this, "expense_service")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("خرج‌یار فعال است")
            .setContentText(text)
            .setOngoing(true)
            .build()

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(10, notification(text))
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        recognizer?.destroy()
        tts?.shutdown()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
