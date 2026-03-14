package com.example.touchlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var permissionManager: PermissionManager
    private lateinit var btnEnable: MaterialButton
    private var isServiceRunning = false

    private val serviceStoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isServiceRunning = false
            btnEnable.text = getString(R.string.btn_enable_floating_menu)
            btnEnable.setIconResource(R.drawable.ic_power)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)
        btnEnable = findViewById(R.id.btnEnable)

        setupQuickSettings()

        btnEnable.setOnClickListener {
            if (isServiceRunning) {
                stopFloatingService()
                isServiceRunning = false
                btnEnable.text = getString(R.string.btn_enable_floating_menu)
                btnEnable.setIconResource(R.drawable.ic_power)
            } else {
                if (permissionManager.hasOverlayPermission()) {
                    startFloatingService()
                    isServiceRunning = true
                    btnEnable.text = getString(R.string.btn_disable_floating_menu)
                    btnEnable.setIconResource(R.drawable.ic_close)
                } else {
                    permissionManager.requestOverlayPermission(this)
                }
            }
        }

        // Register for service stop broadcasts
        val filter = IntentFilter(FloatingMenuService.ACTION_SERVICE_STOPPED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceStoppedReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(serviceStoppedReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(serviceStoppedReceiver)
    }

    private fun setupQuickSettings() {
        setupCard(R.id.qsSensitivity, R.drawable.ic_fingerprint, "Sensitivity", "Medium (0.5s)")
        setupCard(R.id.qsHaptics, R.drawable.ic_fingerprint, "Haptics", "Enabled")
        setupCard(R.id.qsOpacity, R.drawable.ic_fingerprint, "Opacity", "40% Alpha")
        setupCard(R.id.qsAutoLock, R.drawable.ic_lock, "Auto-Lock", "Proximity only")
    }

    private fun setupCard(id: Int, iconRes: Int, title: String, subtitle: String) {
        val card = findViewById<android.view.View>(id) ?: return
        card.findViewById<ImageView>(R.id.qsIcon)?.setImageResource(iconRes)
        card.findViewById<TextView>(R.id.qsTitle)?.text = title
        card.findViewById<TextView>(R.id.qsSubtitle)?.text = subtitle
        card.setOnClickListener {
            Toast.makeText(this, "$title setting tapped", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionManager.OVERLAY_PERMISSION_REQ_CODE) {
            if (permissionManager.hasOverlayPermission()) {
                startFloatingService()
                isServiceRunning = true
                btnEnable.text = getString(R.string.btn_disable_floating_menu)
                btnEnable.setIconResource(R.drawable.ic_close)
            } else {
                Toast.makeText(this, "Permission denied. Cannot show floating menu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingMenuService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingService() {
        stopService(Intent(this, FloatingMenuService::class.java))
    }
}
