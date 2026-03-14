package com.example.touchlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var permissionManager: PermissionManager
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)

        val btnEnable = findViewById<MaterialButton>(R.id.btnEnable)

        // Set up quick settings display
        setupQuickSettings()

        btnEnable.setOnClickListener {
            if (isServiceRunning) {
                stopFloatingService()
                btnEnable.text = getString(R.string.btn_enable_floating_menu)
                btnEnable.setIconResource(R.drawable.ic_power)
                isServiceRunning = false
            } else {
                if (permissionManager.hasOverlayPermission()) {
                    startFloatingService()
                    btnEnable.text = getString(R.string.btn_disable_floating_menu)
                    btnEnable.setIconResource(R.drawable.ic_close)
                    isServiceRunning = true
                } else {
                    permissionManager.requestOverlayPermission(this)
                }
            }
        }
    }

    private fun setupQuickSettings() {
        // Sensitivity card
        val qsSensitivity = findViewById<android.view.View>(R.id.qsSensitivity)
        qsSensitivity?.findViewById<ImageView>(R.id.qsIcon)?.setImageResource(R.drawable.ic_fingerprint)
        qsSensitivity?.findViewById<TextView>(R.id.qsTitle)?.text = getString(R.string.qs_sensitivity)
        qsSensitivity?.findViewById<TextView>(R.id.qsSubtitle)?.text = "Medium (0.5s)"

        // Haptics card
        val qsHaptics = findViewById<android.view.View>(R.id.qsHaptics)
        qsHaptics?.findViewById<ImageView>(R.id.qsIcon)?.setImageResource(R.drawable.ic_fingerprint)
        qsHaptics?.findViewById<TextView>(R.id.qsTitle)?.text = getString(R.string.qs_haptics)
        qsHaptics?.findViewById<TextView>(R.id.qsSubtitle)?.text = "Enabled"

        // Opacity card
        val qsOpacity = findViewById<android.view.View>(R.id.qsOpacity)
        qsOpacity?.findViewById<ImageView>(R.id.qsIcon)?.setImageResource(R.drawable.ic_fingerprint)
        qsOpacity?.findViewById<TextView>(R.id.qsTitle)?.text = getString(R.string.qs_opacity)
        qsOpacity?.findViewById<TextView>(R.id.qsSubtitle)?.text = "40% Alpha"

        // Auto-Lock card
        val qsAutoLock = findViewById<android.view.View>(R.id.qsAutoLock)
        qsAutoLock?.findViewById<ImageView>(R.id.qsIcon)?.setImageResource(R.drawable.ic_lock)
        qsAutoLock?.findViewById<TextView>(R.id.qsTitle)?.text = getString(R.string.qs_auto_lock)
        qsAutoLock?.findViewById<TextView>(R.id.qsSubtitle)?.text = "Proximity only"
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionManager.OVERLAY_PERMISSION_REQ_CODE) {
            if (permissionManager.hasOverlayPermission()) {
                startFloatingService()
                isServiceRunning = true
                val btnEnable = findViewById<MaterialButton>(R.id.btnEnable)
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
