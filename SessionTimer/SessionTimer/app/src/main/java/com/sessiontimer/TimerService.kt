package com.sessiontimer

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TimerService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private val CHANNEL_ID = "session_timer_channel"
    private val ALERT_CHANNEL_ID = "session_timer_alert_channel"
    private val NOTIF_ID = 1
    private val ALERT_NOTIF_ID = 2

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationMinutes = intent?.getIntExtra("duration_minutes", 15) ?: 15
        val appName = intent?.getStringExtra("app_name") ?: "App"
        val action = intent?.getStringExtra("action") ?: AppMonitorService.ACTION_GO_HOME

        createNotificationChannels()
        startForeground(NOTIF_ID, buildNotification("$appName — session running", ""))

        val durationMs = durationMinutes * 60 * 1000L

        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                val mins = secondsLeft / 60
                val secs = secondsLeft % 60
                updateNotification(appName, String.format("%d:%02d remaining", mins, secs))
            }

            override fun onFinish() {
                AppMonitorService.isTimerActive = false
                AppMonitorService.activeApp = null

                when (action) {
                    AppMonitorService.ACTION_GO_HOME -> {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(homeIntent)
                        showAlertNotification(appName, "Your $appName session is up! The app is still open in the background.")
                    }
                    AppMonitorService.ACTION_NOTIFY_ONLY -> {
                        showAlertNotification(appName, "Your $appName session timer has ended.")
                    }
                }

                stopSelf()
            }
        }.start()

        return START_NOT_STICKY
    }

    private fun buildNotification(title: String, text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun showAlertNotification(appName: String, message: String) {
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("⏰ Session Timer: $appName")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(ALERT_NOTIF_ID, notification)
    }

    private fun updateNotification(title: String, text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(title, text))
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Session Timer", NotificationManager.IMPORTANCE_LOW)
        )
        nm.createNotificationChannel(
            NotificationChannel(ALERT_CHANNEL_ID, "Session Timer Alerts", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        AppMonitorService.isTimerActive = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
