package com.example.touchlock

class UnlockGestureDetector(private val onUnlockCallback: () -> Unit) {

    private val TAP_TIMEOUT = 2000L // 2 seconds
    private var tapCount = 0
    private var lastTapTime = 0L

    fun onTouchEvent(): Boolean {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastTapTime > TAP_TIMEOUT) {
            tapCount = 1
        } else {
            tapCount++
        }
        
        lastTapTime = currentTime

        if (tapCount == 3) {
            tapCount = 0
            onUnlockCallback()
        }
        
        return true // Always consume the touch
    }
}
