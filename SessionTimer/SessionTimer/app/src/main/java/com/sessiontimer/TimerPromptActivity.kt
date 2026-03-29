package com.sessiontimer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TimerPromptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_prompt)

        val packageName = intent.getStringExtra("package_name") ?: run {
            finish()
            return
        }

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        findViewById<TextView>(R.id.tv_app_name).text = "Opening $appName"

        val minutePicker = findViewById<NumberPicker>(R.id.picker_minutes).apply {
            minValue = 1
            maxValue = 120
            value = 15
        }

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group_action)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            val minutes = minutePicker.value
            val action = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_notify_only -> AppMonitorService.ACTION_NOTIFY_ONLY
                else                   -> AppMonitorService.ACTION_GO_HOME
            }

            AppMonitorService.isTimerActive = true
            AppMonitorService.activeApp = packageName

            val serviceIntent = Intent(this, TimerService::class.java).apply {
                putExtra("duration_minutes", minutes)
                putExtra("package_name", packageName)
                putExtra("app_name", appName)
                putExtra("action", action)
            }
            startForegroundService(serviceIntent)
            finish()
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(homeIntent)
            finish()
        }
    }
}
