package com.shams.notetodo.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmHelper {

    /**
     * تنظیم آلارم برای یک تسک
     * @param context Context برنامه
     * @param taskId شناسه تسک
     * @param title عنوان تسک
     * @param calendar زمان مورد نظر برای زنگ آلارم
     * @return true اگر آلارم با موفقیت تنظیم شد، false در غیر این صورت
     */
    fun scheduleTaskAlarm(context: Context, taskId: Int, title: String, calendar: Calendar): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // اگر زمان آلارم از الان گذشته باشد، تنظیم نکن
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            android.util.Log.e("AlarmHelper", "زمان آلارم از الان گذشته است: ${calendar.timeInMillis}")
            return false
        }

        // ساخت Intent برای AlarmReceiver
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("title", title)
            putExtra("message", "وقت انجام این تسک رسید!")
        }

        // ساخت PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,  // استفاده از taskId به عنوان requestCode برای یکتایی
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // تنظیم آلارم بر اساس نسخه اندروید
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // اگر مجوز دقیق ندارد، از setExact بدون allowWhileIdle استفاده کن
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else // Android 6 - 11
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

            android.util.Log.d("AlarmHelper", "آلارم با موفقیت تنظیم شد - Task ID: $taskId, زمان: ${calendar.time}")
            return true

        } catch (e: Exception) {
            android.util.Log.e("AlarmHelper", "خطا در تنظیم آلارم: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * کنسل کردن آلارم یک تسک
     * @param context Context برنامه
     * @param taskId شناسه تسک
     */
    fun cancelTaskAlarm(context: Context, taskId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            android.util.Log.d("AlarmHelper", "آلارم با موفقیت کنسل شد - Task ID: $taskId")
        } catch (e: Exception) {
            android.util.Log.e("AlarmHelper", "خطا در کنسل کردن آلارم: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * بررسی می‌کند که آیا آلارمی برای این taskId وجود دارد یا نه
     * @param context Context برنامه
     * @param taskId شناسه تسک
     * @return true اگر آلارم وجود داشته باشد، false در غیر این صورت
     */
    fun isAlarmScheduled(context: Context, taskId: Int): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
}