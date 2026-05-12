package com.shams.notetodo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.shams.notetodo.MainActivity
import com.shams.notetodo.R
import com.shams.notetodo.alarm.RingingActivity
import androidx.core.net.toUri

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // دریافت اطلاعات از Intent
        val taskId = intent.getIntExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: "تسک جدید"
        val message = intent.getStringExtra("message") ?: "وقت انجام این تسک رسید!"

        // چک کردن مجوز اعلان برای اندروید 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_channel"

        // ==================== 1. ساخت کانال اعلان ====================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = getCustomSoundUri(context)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                "یادآور تسک‌ها",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "یادآوری زمان انجام تسک‌ها"

                if (soundUri != null) {
                    setSound(soundUri, audioAttributes)
                }

                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 800, 500, 800, 500)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // ==================== 2. Intent برای صفحه آلارم اختصاصی ====================
        val fullScreenIntent = Intent(context, RingingActivity::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("task_title", title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ==================== 3. Intent برای کلیک روی اعلان ====================
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("taskId", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notificationPendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ==================== 4. ساخت اعلان ====================
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // 👈 اینجا
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setVibrate(longArrayOf(0, 500, 800, 500, 800, 500))
            .setSound(getCustomSoundUri(context))
            .build()

        // نمایش اعلان
        notificationManager.notify(taskId, notification)

        // لاگ برای دیباگ
        android.util.Log.d("AlarmReceiver", "Alarm triggered for task: $title (ID: $taskId)")
    }

    private fun getCustomSoundUri(context: Context): Uri? {
        return try {
            val soundResourceId = R.raw.ring
            "${android.content.ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$soundResourceId".toUri()
        } catch (e: Exception) {
            e.printStackTrace()
            Settings.System.DEFAULT_ALARM_ALERT_URI
        }
    }
}