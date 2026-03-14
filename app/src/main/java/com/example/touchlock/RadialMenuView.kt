package com.example.touchlock

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.cos
import kotlin.math.sin
import com.example.touchlock.R

class RadialMenuView(context: Context, private val callbacks: RadialMenuCallbacks) : FrameLayout(context) {

    interface RadialMenuCallbacks {
        fun onLockClicked()
        fun onUnlockClicked()
        fun onMoveClicked()
        fun onCloseClicked()
    }

    private var isMenuExpanded = false
    private val radiusPx = 250 // Expansion radius in pixels
    
    // The main floating button
    private val floatingButton: ImageView
    
    // The menu items
    private val btnLock: ImageView
    private val btnUnlock: ImageView
    private val btnMove: ImageView
    private val btnClose: ImageView

    private val menuItems = mutableListOf<ImageView>()

    init {
        // Set up the main floating button
        floatingButton = createButton(R.drawable.ic_floating_button, 150)
        floatingButton.setOnClickListener {
            toggleMenu()
        }

        // Set up menu items
        btnLock = createButton(R.drawable.ic_lock, 120, Color.parseColor("#E53935")) // Red
        btnUnlock = createButton(R.drawable.ic_unlock, 120, Color.parseColor("#43A047")) // Green
        btnMove = createButton(R.drawable.ic_move, 120, Color.parseColor("#1E88E5")) // Blue
        btnClose = createButton(R.drawable.ic_close, 120, Color.parseColor("#757575")) // Grey

        btnLock.setOnClickListener { callbacks.onLockClicked(); collapseMenu() }
        btnUnlock.setOnClickListener { callbacks.onUnlockClicked(); collapseMenu() }
        btnMove.setOnClickListener { callbacks.onMoveClicked(); collapseMenu() }
        btnClose.setOnClickListener { callbacks.onCloseClicked(); collapseMenu() }

        menuItems.addAll(listOf(btnLock, btnMove, btnUnlock, btnClose))

        // Add views. Menu items first so they are under the main button
        menuItems.forEach { 
            it.visibility = View.GONE
            it.alpha = 0f
            addView(it) 
        }
        addView(floatingButton)
    }

    private fun createButton(iconRes: Int, sizePx: Int, bgColor: Int = Color.TRANSPARENT): ImageView {
        val imageView = ImageView(context)
        imageView.setImageResource(iconRes)
        imageView.setPadding(24, 24, 24, 24)
        
        if (bgColor != Color.TRANSPARENT) {
            val bg = GradientDrawable()
            bg.shape = GradientDrawable.OVAL
            bg.setColor(bgColor)
            imageView.background = bg
        }

        val params = LayoutParams(sizePx, sizePx).apply {
            gravity = Gravity.CENTER
        }
        imageView.layoutParams = params
        imageView.elevation = 10f
        return imageView
    }

    private fun toggleMenu() {
        if (isMenuExpanded) {
            collapseMenu()
        } else {
            expandMenu()
        }
    }

    fun expandMenu() {
        if (isMenuExpanded) return
        isMenuExpanded = true

        val animatorSet = AnimatorSet()
        val animators = mutableListOf<Animator>()

        val angleStep = Math.PI * 2 / menuItems.size

        for ((index, item) in menuItems.withIndex()) {
            item.visibility = View.VISIBLE
            
            // Start from center
            item.translationX = 0f
            item.translationY = 0f
            item.alpha = 0f
            item.scaleX = 0f
            item.scaleY = 0f

            // Calculate target positions based on angle
            val angle = index * angleStep - (Math.PI / 2) // Start from top
            val targetX = (radiusPx * cos(angle)).toFloat()
            val targetY = (radiusPx * sin(angle)).toFloat()

            val moveX = ObjectAnimator.ofFloat(item, "translationX", 0f, targetX)
            val moveY = ObjectAnimator.ofFloat(item, "translationY", 0f, targetY)
            val alpha = ObjectAnimator.ofFloat(item, "alpha", 0f, 1f)
            val scaleX = ObjectAnimator.ofFloat(item, "scaleX", 0f, 1f)
            val scaleY = ObjectAnimator.ofFloat(item, "scaleY", 0f, 1f)

            animators.addAll(listOf(moveX, moveY, alpha, scaleX, scaleY))
        }

        animatorSet.playTogether(animators)
        animatorSet.interpolator = OvershootInterpolator()
        animatorSet.duration = 400
        animatorSet.start()
        
        // Spin the main button a bit
        ObjectAnimator.ofFloat(floatingButton, "rotation", 0f, 45f).setDuration(300).start()
    }

    fun collapseMenu() {
        if (!isMenuExpanded) return
        isMenuExpanded = false

        val animatorSet = AnimatorSet()
        val animators = mutableListOf<Animator>()

        for (item in menuItems) {
            val moveX = ObjectAnimator.ofFloat(item, "translationX", item.translationX, 0f)
            val moveY = ObjectAnimator.ofFloat(item, "translationY", item.translationY, 0f)
            val alpha = ObjectAnimator.ofFloat(item, "alpha", 1f, 0f)
            val scaleX = ObjectAnimator.ofFloat(item, "scaleX", 1f, 0f)
            val scaleY = ObjectAnimator.ofFloat(item, "scaleY", 1f, 0f)

            animators.addAll(listOf(moveX, moveY, alpha, scaleX, scaleY))
        }

        animatorSet.playTogether(animators)
        animatorSet.duration = 250
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                for (item in menuItems) {
                    item.visibility = View.GONE
                }
            }
        })
        animatorSet.start()
        
        // Restore main button rotation
        ObjectAnimator.ofFloat(floatingButton, "rotation", 45f, 0f).setDuration(250).start()
    }
}
