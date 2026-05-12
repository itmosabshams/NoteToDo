package com.shams.notetodo.alarm

import android.app.Activity
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.shams.notetodo.MainActivity
import com.shams.notetodo.ui.theme.NoteToDoTheme
import com.shams.notetodo.utils.AlarmHelper
import java.util.Calendar

class RingingActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var taskTitle: String = ""
    private var taskId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // دریافت اطلاعات تسک از Intent
        taskTitle = intent.getStringExtra("task_title") ?: "تسک جدید"
        taskId = intent.getIntExtra("task_id", -1)

        android.util.Log.d("RingingActivity", "آلارم باز شد - Task ID: $taskId, Title: $taskTitle")

        // تنظیمات صفحه برای نمایش روی قفل صفحه
        setupWindowFlags()

        // گرفتن WakeLock برای روشن نگه داشتن صفحه
        acquireWakeLock()

        setContent {
            NoteToDoTheme {
                RingingScreen(
                    taskTitle = taskTitle,
                    onDismiss = { dismissAlarm() },
                    onSnooze = { snoozeAlarm() }
                )
            }
        }
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // برای نمایش روی صفحه قفل (حتی با کیگارد فعال)
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "NoteToDo:AlarmWakeLock"
        )
        wakeLock?.acquire(30_000L) // 30 ثانیه
    }

    private fun dismissAlarm() {
        android.util.Log.d("RingingActivity", "دکمه خاموش زده شد - Task ID: $taskId")

        // 1. کنسل کردن آلارم
        AlarmHelper.cancelTaskAlarm(this, taskId)

        // 2. بستن نوتیفیکیشن
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId)

        // 3. آزاد کردن WakeLock
        releaseWakeLock()

        // 4. بستن صفحه آلارم
        finish()
    }

    private fun snoozeAlarm() {
        android.util.Log.d("RingingActivity", "دکمه اسنوز زده شد - Task ID: $taskId")

        // 1. کنسل کردن آلارم فعلی
        AlarmHelper.cancelTaskAlarm(this, taskId)

        // 2. تنظیم آلارم جدید برای 5 دقیقه بعد
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)

        android.util.Log.d("RingingActivity", "اسنوز - زمان جدید: ${calendar.time}")

        // 3. تنظیم آلارم جدید
        val success = AlarmHelper.scheduleTaskAlarm(
            this,
            taskId,
            taskTitle,
            calendar
        )

        // 4. بستن نوتیفیکیشن فعلی
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId)

        // 5. نمایش پیام به کاربر
        if (success) {
            Toast.makeText(
                this,
                "⏰ آلارم 5 دقیقه دیگر دوباره زنگ می‌زند",
                Toast.LENGTH_SHORT
            ).show()
            android.util.Log.d("RingingActivity", "اسنوز با موفقیت تنظیم شد")
        } else {
            Toast.makeText(
                this,
                "❌ خطا در تنظیم اسنوز",
                Toast.LENGTH_SHORT
            ).show()
            android.util.Log.e("RingingActivity", "خطا در تنظیم اسنوز")
        }

        // 6. آزاد کردن WakeLock
        releaseWakeLock()

        // 7. بستن صفحه آلارم
        finish()
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                android.util.Log.d("RingingActivity", "WakeLock آزاد شد")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }
}