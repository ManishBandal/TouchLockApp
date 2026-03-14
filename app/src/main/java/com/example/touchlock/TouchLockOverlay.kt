package com.example.touchlock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class TouchLockOverlay(
    private val context: Context,
    private val onUnlock: () -> Unit
) {

    private var windowManager: WindowManager? = null
    private var overlayView: FrameLayout? = null

    private val gestureDetector = UnlockGestureDetector {
        Toast.makeText(context, "Touch Lock Disabled", Toast.LENGTH_SHORT).show()
        onUnlock()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (overlayView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView = FrameLayout(context).apply {
            setBackgroundColor(0x00000000) // Transparent

            // Lock indicator icon in the top-left
            val icon = ImageView(context).apply {
                setImageResource(R.drawable.ic_lock)
                alpha = 0.5f
            }
            val iconLp = FrameLayout.LayoutParams(80, 80).apply {
                gravity = Gravity.TOP or Gravity.START
                setMargins(40, 40, 0, 0)
            }
            addView(icon, iconLp)

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    gestureDetector.onTap()
                }
                true // Consume all touches
            }
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager?.addView(overlayView, lp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun remove() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }
}
