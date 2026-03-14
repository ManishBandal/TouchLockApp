package com.example.touchlock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.abs
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
        fun onDrag(deltaX: Int, deltaY: Int)
        fun onDragEnd()
    }

    private var isExpanded = false
    private val menuRadius = 220f
    private val mainButtonSize = 160
    private val itemButtonSize = 120

    val mainButton: ImageView
    private val menuItems = mutableListOf<ImageView>()

    private var isDragMode = false
    private var downX = 0f
    private var downY = 0f
    private var hasMoved = false
    private val touchSlop = 15f

    private data class MenuItemData(val iconRes: Int, val bgColor: Int)
    private val menuData = listOf(
        MenuItemData(R.drawable.ic_lock, Color.parseColor("#f49d25")),
        MenuItemData(R.drawable.ic_unlock, Color.parseColor("#43A047")),
        MenuItemData(R.drawable.ic_move, Color.parseColor("#1E88E5")),
        MenuItemData(R.drawable.ic_close, Color.parseColor("#757575"))
    )

    init {
        for (data in menuData) {
            val btn = ImageView(context).apply {
                setImageResource(data.iconRes)
                setPadding(24, 24, 24, 24)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(data.bgColor)
                }
                elevation = 8f
                visibility = View.GONE
                alpha = 0f
                scaleX = 0f
                scaleY = 0f
            }
            addView(btn, LayoutParams(itemButtonSize, itemButtonSize).apply {
                gravity = Gravity.CENTER
            })
            menuItems.add(btn)
        }

        menuItems[0].setOnClickListener { callbacks.onLockClicked(); collapse() }
        menuItems[1].setOnClickListener { callbacks.onUnlockClicked(); collapse() }
        menuItems[2].setOnClickListener { callbacks.onMoveClicked(); collapse() }
        menuItems[3].setOnClickListener { callbacks.onPauseClicked(); collapse() }

        mainButton = ImageView(context).apply {
            setImageResource(R.drawable.ic_floating_button)
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#f49d25"))
            }
            elevation = 16f
        }
        addView(mainButton, LayoutParams(mainButtonSize, mainButtonSize).apply {
            gravity = Gravity.CENTER
        })

        setupMainButtonTouch()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMainButtonTouch() {
        mainButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    hasMoved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop || hasMoved) {
                        hasMoved = true
                        callbacks.onDrag(dx.toInt(), dy.toInt())
                        downX = event.rawX
                        downY = event.rawY
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (hasMoved) {
                        callbacks.onDragEnd()
                    } else {
                        toggle()
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun enableDragMode() {
        isDragMode = true
    }

    private fun toggle() {
        if (isExpanded) collapse() else expand()
    }

    fun expand() {
        if (isExpanded) return
        isExpanded = true

        val angleStep = (2.0 * Math.PI) / menuItems.size

        for ((index, item) in menuItems.withIndex()) {
            val angle = index * angleStep - (Math.PI / 2.0)
            val targetX = (menuRadius * cos(angle)).toFloat()
            val targetY = (menuRadius * sin(angle)).toFloat()

            item.visibility = View.VISIBLE
            item.translationX = 0f
            item.translationY = 0f
            item.alpha = 0f
            item.scaleX = 0f
            item.scaleY = 0f

            item.animate()
                .translationX(targetX).translationY(targetY)
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(300)
                .setStartDelay((index * 30).toLong())
                .start()
        }

        mainButton.animate().rotation(45f).setDuration(250).start()
    }

    fun collapse() {
        if (!isExpanded) return
        isExpanded = false

        for (item in menuItems) {
            item.animate()
                .translationX(0f).translationY(0f)
                .alpha(0f).scaleX(0f).scaleY(0f)
                .setDuration(200)
                .withEndAction { item.visibility = View.GONE }
                .start()
        }

        mainButton.animate().rotation(0f).setDuration(200).start()
    }
}
