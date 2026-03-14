package com.example.touchlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit val permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        // Update instructions for radial menu
        val instructionsTextView = findViewById<android.widget.TextView>(R.id.instructionsText)
        instructionsTextView?.text = "Press Start to launch the floating control button."

        btnStart.setOnClickListener {
            if (permissionManager.hasOverlayPermission()) {
                startFloatingMenuService()
            } else {
                permissionManager.requestOverlayPermission()
            }
        }

        btnStop.setOnClickListener {
            stopFloatingMenuService()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionManager.OVERLAY_PERMISSION_REQ_CODE) {
            if (permissionManager.hasOverlayPermission()) {
                startFloatingMenuService()
            } else {
                Toast.makeText(this, "Permission denied. App cannot work.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startFloatingMenuService() {
        // We will stop the old overlay service if it's running just in case
        stopService(Intent(this, OverlayService::class.java))
        
        val intent = Intent(this, FloatingMenuService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingMenuService() {
        val intent = Intent(this, FloatingMenuService::class.java)
        stopService(intent)
        // Also stop legacy service just in case
        stopService(Intent(this, OverlayService::class.java))
    }
}
