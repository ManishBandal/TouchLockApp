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
import com.example.touchlock.R

class FloatingMenuService : Service(), RadialMenuView.RadialMenuCallbacks {

    private val CHANNEL_ID = "FloatingMenuChannel"
    private val NOTIFICATION_ID = 2
    
    private var windowManager: WindowManager? = null
    private var radialMenuView: RadialMenuView? = null
    private var touchLockOverlay: TouchLockOverlay? = null
    
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var sharedPreferences: SharedPreferences

    private val PREFS_NAME = "TouchLockPrefs"
    private val PREF_X = "FloatingButtonX"
    private val PREF_Y = "FloatingButtonY"

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDraggingEnabled = false

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        setupFloatingMenu()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingMenu()
        touchLockOverlay?.remove()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Floating Menu Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Touch Lock Active")
            .setContentText("Tap to manage touch lock")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingMenu() {
        radialMenuView = RadialMenuView(this, this)
        
        // Large box so the expanded menu isn't clipped
        layoutParams = WindowManager.LayoutParams(
            800, 800,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = sharedPreferences.getInt(PREF_X, 0)
        layoutParams.y = sharedPreferences.getInt(PREF_Y, 200)

        radialMenuView?.setOnTouchListener { view, event ->
            if (isDraggingEnabled) {
                handleDrag(event)
            } else {
                false // Let the button handle clicks
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
                // Snap to edges horizontally
                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                
                if (layoutParams.x > screenWidth / 2) {
                    layoutParams.x = screenWidth // right edge
                } else {
                    layoutParams.x = 0 // left edge
                }
                windowManager?.updateViewLayout(radialMenuView, layoutParams)

                // Save persistence
                sharedPreferences.edit()
                    .putInt(PREF_X, layoutParams.x)
                    .putInt(PREF_Y, layoutParams.y)
                    .apply()
                
                // Disable drag mode after dragging once
                isDraggingEnabled = false
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

    // Radial Menu Callbacks
    override fun onLockClicked() {
        if (touchLockOverlay == null) {
            touchLockOverlay = TouchLockOverlay(this) {
                // Callback when triple-tap is detected
                touchLockOverlay?.remove()
                touchLockOverlay = null
                
                // Show the floating menu again if we hid it
                radialMenuView?.visibility = View.VISIBLE
            }
        }
        
        touchLockOverlay?.show()
        // Hide the floating menu while screen is locked
        radialMenuView?.visibility = View.GONE
    }

    override fun onUnlockClicked() {
        Toast.makeText(this, "Screen is already unlocked", Toast.LENGTH_SHORT).show()
    }

    override fun onMoveClicked() {
        isDraggingEnabled = true
        Toast.makeText(this, "Drag the button to move it", Toast.LENGTH_SHORT).show()
    }

    override fun onCloseClicked() {
        stopSelf()
    }
}
