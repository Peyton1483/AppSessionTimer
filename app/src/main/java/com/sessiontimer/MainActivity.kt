package com.sessiontimer

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isAccessibilityEnabled()) {
            Toast.makeText(
                this,
                "Please enable Session Timer in Accessibility Settings",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        val prefs = getSharedPreferences(AppMonitorService.PREFS_NAME, MODE_PRIVATE)
        val saved = prefs.getStringSet(AppMonitorService.KEY_MONITORED_APPS, emptySet()) ?: emptySet()

        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter {
                it.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    && it.packageName != packageName
            }
            .map {
                AppItem(
                    pm.getApplicationLabel(it).toString(),
                    it.packageName,
                    it.packageName in saved
                )
            }
            .sortedBy { it.name }

        val adapter = AppAdapter(apps)
        val recycler = findViewById<RecyclerView>(R.id.recycler_apps)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val selected = adapter.getSelectedPackages()
            prefs.edit().putStringSet(AppMonitorService.KEY_MONITORED_APPS, selected).apply()
            Toast.makeText(this, "Saved! ${selected.size} app(s) monitored.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(packageName)
    }
}
