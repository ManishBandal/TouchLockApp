package com.example.touchlock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("ViewConstructor")
class RadialMenuView(
    context: Context,
    private val callbacks: MenuCallbacks
) : FrameLayout(context) {

    interface MenuCallbacks {
        fun onLockClicked()
        fun onUnlockClicked()
        fun onMoveClicked()
        fun onPauseClicked()
    }

    private var isExpanded = false
    private val menuRadius = 220f // px from center
    private val mainButtonSize = 160
    private val itemButtonSize = 120

    private val mainButton: ImageView
    private val menuItems = mutableListOf<ImageView>()

    // Menu item data: icon resource, tint color
    private data class MenuItem(val iconRes: Int, val bgColor: Int)
    private val menuData = listOf(
        MenuItem(R.drawable.ic_lock, Color.parseColor("#f49d25")),     // Lock
        MenuItem(R.drawable.ic_unlock, Color.parseColor("#43A047")),   // Unlock
        MenuItem(R.drawable.ic_move, Color.parseColor("#1E88E5")),     // Move
        MenuItem(R.drawable.ic_close, Color.parseColor("#757575"))     // Pause
    )

    init {
        // Create menu item buttons first (below main button in z-order)
        for (data in menuData) {
            val btn = ImageView(context).apply {
                setImageResource(data.iconRes)
                setPadding(24, 24, 24, 24)
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(data.bgColor)
                }
                background = bg
                elevation = 8f
                visibility = View.GONE
                alpha = 0f
                scaleX = 0f
                scaleY = 0f
            }
            val lp = LayoutParams(itemButtonSize, itemButtonSize).apply {
                gravity = Gravity.CENTER
            }
            addView(btn, lp)
            menuItems.add(btn)
        }

        // Set click listeners
        menuItems[0].setOnClickListener { callbacks.onLockClicked(); collapse() }
        menuItems[1].setOnClickListener { callbacks.onUnlockClicked(); collapse() }
        menuItems[2].setOnClickListener { callbacks.onMoveClicked(); collapse() }
        menuItems[3].setOnClickListener { callbacks.onPauseClicked(); collapse() }

        // Create main floating button (on top)
        mainButton = ImageView(context).apply {
            setImageResource(R.drawable.ic_floating_button)
            setPadding(32, 32, 32, 32)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#f49d25"))
            }
            background = bg
            elevation = 16f
        }
        val mainLp = LayoutParams(mainButtonSize, mainButtonSize).apply {
            gravity = Gravity.CENTER
        }
        addView(mainButton, mainLp)

        mainButton.setOnClickListener { toggle() }
    }

    private fun toggle() {
        if (isExpanded) collapse() else expand()
    }

    fun expand() {
        if (isExpanded) return
        isExpanded = true

        val angleStep = (2.0 * Math.PI) / menuItems.size

        for ((index, item) in menuItems.withIndex()) {
            val angle = index * angleStep - (Math.PI / 2.0) // start from top
            val targetX = (menuRadius * cos(angle)).toFloat()
            val targetY = (menuRadius * sin(angle)).toFloat()

            item.visibility = View.VISIBLE
            item.translationX = 0f
            item.translationY = 0f
            item.alpha = 0f
            item.scaleX = 0f
            item.scaleY = 0f

            item.animate()
                .translationX(targetX)
                .translationY(targetY)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay((index * 30).toLong())
                .start()
        }

        mainButton.animate()
            .rotation(45f)
            .setDuration(250)
            .start()
    }

    fun collapse() {
        if (!isExpanded) return
        isExpanded = false

        for (item in menuItems) {
            item.animate()
                .translationX(0f)
                .translationY(0f)
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction { item.visibility = View.GONE }
                .start()
        }

        mainButton.animate()
            .rotation(0f)
            .setDuration(200)
            .start()
    }
}
