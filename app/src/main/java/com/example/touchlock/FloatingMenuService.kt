package com.example.touchlock

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

class FloatingMenuService : Service(), RadialMenuView.MenuCallbacks {

    companion object {
        private const val CHANNEL_ID = "TouchLockChannel"
        private const val NOTIFICATION_ID = 101
        private const val PREFS_NAME = "TouchLockPrefs"
        private const val PREF_X = "btn_x"
        private const val PREF_Y = "btn_y"
        const val ACTION_SERVICE_STOPPED = "com.example.touchlock.SERVICE_STOPPED"
    }

    private var windowManager: WindowManager? = null
    private var radialMenuView: RadialMenuView? = null
    private var touchLockOverlay: TouchLockOverlay? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var prefs: SharedPreferences
    private var isLocked = false

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        addFloatingMenu()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingMenu()
        touchLockOverlay?.remove()

        // Notify MainActivity that service stopped
        val intent = Intent(ACTION_SERVICE_STOPPED)
        sendBroadcast(intent)
    }

    // --- Notification ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Touch Lock Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Touch Lock Running")
            .setContentText("Tap to open controls")
            .setSmallIcon(R.drawable.ic_lock)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    // --- Floating Menu ---

    @SuppressLint("ClickableViewAccessibility")
    private fun addFloatingMenu() {
        radialMenuView = RadialMenuView(this, this)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Default position: right side of screen
        val screenWidth = resources.displayMetrics.widthPixels
        val savedX = prefs.getInt(PREF_X, screenWidth - 200)
        val savedY = prefs.getInt(PREF_Y, 300)

        layoutParams = WindowManager.LayoutParams(
            700, 700,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        try {
            windowManager?.addView(radialMenuView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeFloatingMenu() {
        radialMenuView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            radialMenuView = null
        }
    }

    private fun savePosition() {
        prefs.edit()
            .putInt(PREF_X, layoutParams.x)
            .putInt(PREF_Y, layoutParams.y)
            .apply()
    }

    // --- Menu Callbacks ---

    override fun onLockClicked() {
        if (isLocked) return

        isLocked = true
        touchLockOverlay = TouchLockOverlay(this) {
            // Unlock callback from puzzle
            touchLockOverlay?.remove()
            touchLockOverlay = null
            isLocked = false
            radialMenuView?.visibility = View.VISIBLE
        }
        touchLockOverlay?.show()
        // Keep radial menu visible but collapsed so user can tap Unlock
        radialMenuView?.collapse()
    }

    override fun onUnlockClicked() {
        if (isLocked && touchLockOverlay != null) {
            // Show the math puzzle
            touchLockOverlay?.showPuzzle()
        } else {
            Toast.makeText(this, "Screen is already unlocked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMoveClicked() {
        Toast.makeText(this, "Drag the button to reposition", Toast.LENGTH_SHORT).show()
    }

    override fun onPauseClicked() {
        stopSelf()
    }

    override fun onDrag(deltaX: Int, deltaY: Int) {
        layoutParams.x += deltaX
        layoutParams.y += deltaY
        try {
            windowManager?.updateViewLayout(radialMenuView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDragEnd() {
        savePosition()
    }
}
