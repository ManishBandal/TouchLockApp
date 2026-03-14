package com.example.touchlock

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class TouchLockOverlay(private val context: Context, private val onUnlock: () -> Unit) {

    private var windowManager: WindowManager? = null
    private var overlayLayout: FrameLayout? = null
    private val gestureDetector = UnlockGestureDetector {
        Toast.makeText(context, "Touch Lock Disabled", Toast.LENGTH_SHORT).show()
        onUnlock()
    }

    fun show() {
        if (overlayLayout != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayLayout = object : FrameLayout(context) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    gestureDetector.onTouchEvent()
                }
                return true // Consume all touches
            }
        }.apply {
            setBackgroundColor(0x00000000)

            val icon = ImageView(context)
            icon.setImageResource(R.drawable.ic_lock)
            val iconParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            ).apply {
                setMargins(50, 50, 0, 0)
            }
            icon.layoutParams = iconParams
            addView(icon)
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager?.addView(overlayLayout, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun remove() {
        overlayLayout?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayLayout = null
        }
    }
}
