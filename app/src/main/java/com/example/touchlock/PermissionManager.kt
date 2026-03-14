package com.example.touchlock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PermissionManager(private val activity: AppCompatActivity) {

    companion object {
        const val OVERLAY_PERMISSION_REQ_CODE = 1000
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(activity)
        } else {
            true
        }
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            Toast.makeText(activity, "Please grant 'Display over other apps' permission to use the floating menu.", Toast.LENGTH_LONG).show()
        }
    }
}
