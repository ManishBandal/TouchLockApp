package com.example.touchlock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

class TouchLockOverlay(
    private val context: Context,
    private val onUnlock: () -> Unit
) {

    private var windowManager: WindowManager? = null
    private var overlayView: FrameLayout? = null
    private var mathUnlockView: MathUnlockView? = null
    private var isPuzzleVisible = false

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (overlayView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // Semi-transparent black

            // Touch blocker — consumes all touches when puzzle is not visible
            setOnTouchListener { _, _ -> true }
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

    fun showPuzzle() {
        if (overlayView == null || isPuzzleVisible) return
        isPuzzleVisible = true

        mathUnlockView = MathUnlockView(context) {
            // Puzzle solved callback
            onUnlock()
        }

        val puzzleLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        overlayView?.addView(mathUnlockView, puzzleLp)
    }

    fun hidePuzzle() {
        if (!isPuzzleVisible) return
        isPuzzleVisible = false
        mathUnlockView?.let { overlayView?.removeView(it) }
        mathUnlockView = null
    }

    fun remove() {
        isPuzzleVisible = false
        mathUnlockView = null
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
