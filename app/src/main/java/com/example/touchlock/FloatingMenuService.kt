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
import android.view.MotionEvent
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
    }

    private var windowManager: WindowManager? = null
    private var radialMenuView: RadialMenuView? = null
    private var touchLockOverlay: TouchLockOverlay? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var prefs: SharedPreferences

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

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

        layoutParams = WindowManager.LayoutParams(
            700, 700,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt(PREF_X, 0)
            y = prefs.getInt(PREF_Y, 200)
        }

        radialMenuView?.setOnTouchListener { _, event ->
            if (isDragging) {
                handleDrag(event)
            } else {
                false
            }
        }

        try {
            windowManager?.addView(radialMenuView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleDrag(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager?.updateViewLayout(radialMenuView, layoutParams)
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Snap to nearest edge
                val screenWidth = resources.displayMetrics.widthPixels
                layoutParams.x = if (layoutParams.x > screenWidth / 2) screenWidth else 0
                windowManager?.updateViewLayout(radialMenuView, layoutParams)

                prefs.edit()
                    .putInt(PREF_X, layoutParams.x)
                    .putInt(PREF_Y, layoutParams.y)
                    .apply()

                isDragging = false
                Toast.makeText(this, "Position saved", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
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

    // --- Menu Callbacks ---

    override fun onLockClicked() {
        if (touchLockOverlay == null) {
            touchLockOverlay = TouchLockOverlay(this) {
                // Unlock callback
                touchLockOverlay?.remove()
                touchLockOverlay = null
                radialMenuView?.visibility = View.VISIBLE
            }
        }
        touchLockOverlay?.show()
        radialMenuView?.visibility = View.GONE
    }

    override fun onUnlockClicked() {
        if (touchLockOverlay != null) {
            touchLockOverlay?.remove()
            touchLockOverlay = null
            radialMenuView?.visibility = View.VISIBLE
            Toast.makeText(this, "Touch Lock Disabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Screen is already unlocked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMoveClicked() {
        isDragging = true
        Toast.makeText(this, "Drag the button to move it", Toast.LENGTH_SHORT).show()
    }

    override fun onPauseClicked() {
        stopSelf()
    }
}
