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
        wakeLock?.acquire(10_000L) // 10 ثانیه
    }

    private fun dismissAlarm() {
        // 1. کنسل کردن آلارم
        AlarmHelper.cancelTaskAlarm(this, taskId)

        // 2. بستن نوتیفیکیشن
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId)

        // 3. آزاد کردن WakeLock
        wakeLock?.let {
            if (it.isHeld) it.release()
        }

        // 4. بستن صفحه آلارم
        finish()
    }

    private fun snoozeAlarm() {
        // 1. کنسل کردن آلارم فعلی
        AlarmHelper.cancelTaskAlarm(this, taskId)

        // 2. تنظیم آلارم جدید برای 5 دقیقه بعد
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 5) // 5 دقیقه اسنوز

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

        // 5. اگر اسنوز موفق بود، یک نوتیفیکیشن کوچیک نشون بده
        if (success) {
            android.widget.Toast.makeText(
                this,
                "⏰ آلارم 5 دقیقه دیگر دوباره زنگ می‌زند",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // 6. آزاد کردن WakeLock
        wakeLock?.let {
            if (it.isHeld) it.release()
        }

        // 7. بستن صفحه آلارم
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }
}