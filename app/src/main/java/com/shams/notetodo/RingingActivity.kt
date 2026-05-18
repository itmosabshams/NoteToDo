package com.shams.notetodo.alarm

import android.app.Activity
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shams.notetodo.ui.theme.NoteToDoTheme
import com.shams.notetodo.utils.AlarmHelper
import java.util.Calendar

class RingingActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var originalAlarmVolume: Int = 0
    private var originalAlarmMode: Int = 0
    private var taskTitle: String = ""
    private var taskId: Int = -1
    private var taskDescription: String = ""

    companion object {
        private const val WAKE_LOCK_TIMEOUT = 120_000L
        private const val EXTRA_TASK_TITLE = "task_title"
        private const val EXTRA_TASK_ID = "task_id"
        private const val EXTRA_TASK_DESCRIPTION = "task_description"

        fun newIntent(
            context: Context,
            taskId: Int,
            taskTitle: String,
            taskDescription: String = ""
        ): Intent {
            return Intent(context, RingingActivity::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
                putExtra(EXTRA_TASK_DESCRIPTION, taskDescription)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "تسک جدید"
        taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION) ?: ""

        android.util.Log.d("RingingActivity", "Alarm opened - Task ID: $taskId")

        if (!isAlarmValid()) {
            finish()
            return
        }

        // 1. ابتدا صدای سیستم رو کنترل کن
        setupAudioControl()

        // 2. تنظیمات صفحه
        setupWindowFlags()

        // 3. روشن کردن صفحه
        acquireWakeLock()

        // 4. پخش صدای اختصاصی
        startAlarmSound()

        // 5. شروع ویبریشن
        startVibration()

        setContent {
            NoteToDoTheme {
                RingingScreen(
                    taskTitle = taskTitle,
                    taskDescription = taskDescription,
                    onDismiss = { dismissAlarm() },
                    onSnooze = { showSnoozeDialog() }
                )
            }
        }
    }

    private fun setupAudioControl() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // ذخیره تنظیمات اصلی آلارم
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            originalAlarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0
            originalAlarmMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        } else {
            @Suppress("DEPRECATION")
            originalAlarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0
            @Suppress("DEPRECATION")
            originalAlarmMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        }

        // غیرفعال کردن حالت سایلنت/ویبره برای آلارم
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager?.ringerMode = AudioManager.RINGER_MODE_NORMAL
        } else {
            @Suppress("DEPRECATION")
            audioManager?.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }

        // افزایش صدای آلارم به حداکثر
        val maxAlarmVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 15
        audioManager?.setStreamVolume(
            AudioManager.STREAM_ALARM,
            maxAlarmVolume,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )

        // گرفتن AudioFocus برای جلوگیری از تداخل با صدای سیستم
        requestAudioFocus()

        android.util.Log.d("RingingActivity", "Audio control setup - Volume: $maxAlarmVolume")
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            android.util.Log.w("RingingActivity", "Audio focus lost")
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            // کاهش موقت صدا
                            mediaPlayer?.setVolume(0.3f, 0.3f)
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            // برگشت صدا به حالت عادی
                            mediaPlayer?.setVolume(1f, 1f)
                        }
                    }
                }
                .build()

            audioFocusRequest?.let {
                audioManager?.requestAudioFocus(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }

        // برگردوندن تنظیمات اصلی آلارم
        audioManager?.setStreamVolume(
            AudioManager.STREAM_ALARM,
            originalAlarmVolume,
            AudioManager.FLAG_ALLOW_RINGER_MODES
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager?.ringerMode = originalAlarmMode
        } else {
            @Suppress("DEPRECATION")
            audioManager?.ringerMode = originalAlarmMode
        }

        android.util.Log.d("RingingActivity", "Audio focus released")
    }

    private fun setupWindowFlags() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            else -> {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                )
            }
        }

        tryDismissKeyguard()
        setMaxBrightness()
    }

    private fun tryDismissKeyguard() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    private fun setMaxBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1.0f
        window.attributes = layoutParams
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "NoteToDo:AlarmWakeLock"
            )
            wakeLock?.acquire(WAKE_LOCK_TIMEOUT)
        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error acquiring wake lock", e)
        }
    }

    private fun startAlarmSound() {
        try {
            // استفاده از صدای اختصاصی اپلیکیشن (اگر دارید)
            var alarmUri: Uri? = null

            // اول سعی کن صدای اختصاصی اپ رو پیدا کنی
            val customSoundId = resources.getIdentifier("alarm_sound", "raw", packageName)
            if (customSoundId != 0) {
                alarmUri = Uri.parse("android.resource://${packageName}/$customSoundId")
            }

            // اگر صدای اختصاصی نداشت، از صدای پیش‌فرض آلارم استفاده کن
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            // اگر صدای آلارم هم نبود، از نوتیفیکیشن استفاده کن
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                setDataSource(applicationContext, alarmUri)
                isLooping = true
                prepare()
                // تنظیم حجم بالا
                setVolume(1f, 1f)
                start()
            }

            android.util.Log.d("RingingActivity", "Alarm sound started - URI: $alarmUri")

        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error playing alarm sound", e)
            tryAlternativeSound()
        }
    }

    private fun tryAlternativeSound() {
        try {
            // استفاده از صدای داخلی به عنوان fallback
            val fallbackSound = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } else {
                @Suppress("DEPRECATION")
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, fallbackSound)
                isLooping = true
                prepare()
                setVolume(1f, 1f)
                start()
            }
        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error playing fallback sound", e)
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(
                        0, 500, 400, 500, 400, 500,
                        800,
                        0, 1000,
                        500,
                        0, 300, 300, 300, 300, 300
                    )
                    val amplitudes = intArrayOf(
                        0, 255, 128, 255, 128, 255,
                        0,
                        0, 255,
                        0,
                        0, 200, 200, 200, 200, 200
                    )
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 500, 500, 500, 500, 500), 0)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error starting vibration", e)
        }
    }

    private fun dismissAlarm() {
        // توقف همه چیز
        stopAlarmSound()
        stopVibration()
        releaseAudioFocus() // برگردوندن صدای سیستم به حالت اول

        AlarmHelper.cancelTaskAlarm(this, taskId)
        cancelNotification()
        releaseWakeLock()

        finish()
    }

    private fun showSnoozeDialog() {
        // دیالوگ اسنوز در RingingScreen مدیریت میشه
    }

    fun snoozeAlarm(minutes: Int = 5) {
        // توقف صداها
        stopAlarmSound()
        stopVibration()
        releaseAudioFocus() // برگردوندن صدای سیستم

        // تنظیم آلارم جدید
        AlarmHelper.cancelTaskAlarm(this, taskId)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutes)

        AlarmHelper.scheduleTaskAlarm(
            this,
            taskId,
            taskTitle,
            calendar
        )

        cancelNotification()
        releaseWakeLock()
        finish()
    }

    private fun stopAlarmSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error stopping alarm sound", e)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error stopping vibration", e)
        }
    }

    private fun cancelNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId)
        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error cancelling notification", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            android.util.Log.e("RingingActivity", "Error releasing wake lock", e)
        }
    }

    private fun isAlarmValid(): Boolean {
        return taskId != -1
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        // غیرفعال کردن دکمه بازگشت
        return
    }

    override fun onDestroy() {
        super.onDestroy()

        // اطمینان از آزاد شدن همه منابع
        if (mediaPlayer?.isPlaying == true) {
            stopAlarmSound()
        }

        if (wakeLock?.isHeld == true) {
            releaseWakeLock()
        }

        if (vibrator != null) {
            stopVibration()
        }

        // مهم: برگردوندن صدای سیستم
        releaseAudioFocus()
    }
}