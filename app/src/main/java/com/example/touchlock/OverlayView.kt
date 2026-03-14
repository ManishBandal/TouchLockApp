package com.example.touchlock

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class OverlayView(context: Context, private val onUnlockCallback: () -> Unit) : FrameLayout(context) {

    private val TAP_TIMEOUT = 2000L // 2 seconds
    private var tapCount = 0
    private var lastTapTime = 0L

    init {
        setBackgroundColor(0x00000000)

        val icon = ImageView(context)
        icon.setImageResource(R.drawable.ic_lock)
        
        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        )
        params.setMargins(50, 50, 0, 0)
        
        icon.layoutParams = params
        addView(icon)
        
        isClickable = true
        isFocusable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastTapTime > TAP_TIMEOUT) {
                tapCount = 1
            } else {
                tapCount++
            }
            
            lastTapTime = currentTime

            if (tapCount == 3) {
                tapCount = 0
                Toast.makeText(context, "Touch Lock Disabled", Toast.LENGTH_SHORT).show()
                onUnlockCallback()
            }
        }
        
        return true
    }
}
