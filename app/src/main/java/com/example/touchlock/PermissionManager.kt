package com.example.touchlock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

class PermissionManager(private val context: Context) {

    companion object {
        const val OVERLAY_PERMISSION_REQ_CODE = 1000
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(context: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            Toast.makeText(
                context,
                "Please grant 'Display over other apps' permission.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
