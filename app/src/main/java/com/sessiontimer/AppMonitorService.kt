package com.sessiontimer

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppMonitorService : AccessibilityService() {

    companion object {
        const val PREFS_NAME = "session_timer_prefs"
        const val KEY_MONITORED_APPS = "monitored_apps"
        // Action values
        const val ACTION_GO_HOME = "go_home"
        const val ACTION_NOTIFY_ONLY = "notify_only"
        var isTimerActive = false
        var activeApp: String? = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app
        if (packageName == applicationContext.packageName) return

        // Don't re-prompt if a timer is already running for this app
        if (isTimerActive && activeApp == packageName) return

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val monitoredApps = prefs.getStringSet(KEY_MONITORED_APPS, emptySet()) ?: emptySet()

        if (packageName in monitoredApps && !isTimerActive) {
            val intent = Intent(this, TimerPromptActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                putExtra("package_name", packageName)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}
}
