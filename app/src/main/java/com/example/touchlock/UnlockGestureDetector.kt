package com.example.touchlock

class UnlockGestureDetector(private val onUnlockCallback: () -> Unit) {

    private val TAP_TIMEOUT = 2000L
    private var tapCount = 0
    private var lastTapTime = 0L

    fun onTap() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTapTime > TAP_TIMEOUT) {
            tapCount = 1
        } else {
            tapCount++
        }

        lastTapTime = currentTime

        if (tapCount >= 3) {
            tapCount = 0
            onUnlockCallback()
        }
    }
}
