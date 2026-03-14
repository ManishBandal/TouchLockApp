package com.example.touchlock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

@SuppressLint("ViewConstructor")
class MathUnlockView(
    context: Context,
    private val onUnlocked: () -> Unit
) : LinearLayout(context) {

    private val gridButtons = mutableListOf<TextView>()
    private val selectedIndices = mutableListOf<Int>()
    private var targetSum = 0
    private var gridNumbers = intArrayOf()
    private lateinit var targetLabel: TextView
    private lateinit var selectionLabel: TextView
    private val maxSelections = 4

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(48, 48, 48, 48)

        // Semi-transparent dark background
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor("#DD1a1a2e"))
            cornerRadius = 32f
        }
        background = bg

        buildUI()
        generatePuzzle()
    }

    private fun buildUI() {
        // Title
        val title = TextView(context).apply {
            text = "Touch Locked"
            setTextColor(Color.WHITE)
            textSize = 22f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        addView(title, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = 8
        })

        // Instruction
        val instruction = TextView(context).apply {
            text = "Tap numbers that add up to the target"
            setTextColor(Color.parseColor("#94a3b8"))
            textSize = 13f
            gravity = Gravity.CENTER
        }
        addView(instruction, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = 24
        })

        // Target display
        targetLabel = TextView(context).apply {
            text = "Target: 0"
            setTextColor(Color.parseColor("#f49d25"))
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        addView(targetLabel, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = 24
        })

        // 3x3 Grid
        val grid = GridLayout(context).apply {
            columnCount = 3
            rowCount = 3
        }
        val cellSize = 180
        val cellMargin = 12

        for (i in 0 until 9) {
            val btn = TextView(context).apply {
                text = "0"
                setTextColor(Color.WHITE)
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = createCellBg(false)
                isClickable = true
                isFocusable = true
            }
            val lp = GridLayout.LayoutParams().apply {
                width = cellSize
                height = cellSize
                setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
            }
            val index = i
            btn.setOnClickListener { onCellTapped(index) }
            grid.addView(btn, lp)
            gridButtons.add(btn)
        }

        val gridLp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
            bottomMargin = 24
        }
        addView(grid, gridLp)

        // Selection display
        selectionLabel = TextView(context).apply {
            text = "Selected: —"
            setTextColor(Color.parseColor("#cbd5e1"))
            textSize = 14f
            gravity = Gravity.CENTER
        }
        addView(selectionLabel, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun createCellBg(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            if (selected) {
                setColor(Color.parseColor("#f49d25"))
                setStroke(3, Color.parseColor("#fbbf24"))
            } else {
                setColor(Color.parseColor("#334155"))
                setStroke(2, Color.parseColor("#475569"))
            }
        }
    }

    fun generatePuzzle() {
        selectedIndices.clear()

        // Shuffle 1-9
        val nums = (1..9).toMutableList()
        nums.shuffle()
        gridNumbers = nums.toIntArray()

        // Generate target that has at least one valid combination
        targetSum = findValidTarget(gridNumbers)

        // Update UI
        targetLabel.text = "Target: $targetSum"
        selectionLabel.text = "Selected: —"

        for (i in 0 until 9) {
            gridButtons[i].text = gridNumbers[i].toString()
            gridButtons[i].background = createCellBg(false)
        }
    }

    private fun findValidTarget(nums: IntArray): Int {
        // Try random targets between 12-20 and verify at least one combo exists
        val candidates = (12..20).toMutableList()
        candidates.shuffle()
        for (target in candidates) {
            if (hasValidCombo(nums, target)) return target
        }
        // Fallback: sum of first two numbers
        return nums[0] + nums[1] + nums[2]
    }

    private fun hasValidCombo(nums: IntArray, target: Int): Boolean {
        // Check all combos of 2, 3, or 4 numbers
        val n = nums.size
        for (i in 0 until n) {
            if (nums[i] == target) return true
            for (j in i + 1 until n) {
                val s2 = nums[i] + nums[j]
                if (s2 == target) return true
                for (k in j + 1 until n) {
                    val s3 = s2 + nums[k]
                    if (s3 == target) return true
                    for (l in k + 1 until n) {
                        if (s3 + nums[l] == target) return true
                    }
                }
            }
        }
        return false
    }

    private fun onCellTapped(index: Int) {
        if (selectedIndices.contains(index)) {
            // Deselect
            selectedIndices.remove(index)
            gridButtons[index].background = createCellBg(false)
        } else {
            if (selectedIndices.size >= maxSelections) {
                // Reset all
                resetSelection()
                Toast.makeText(context, "Max 4 numbers. Try again.", Toast.LENGTH_SHORT).show()
                return
            }
            selectedIndices.add(index)
            gridButtons[index].background = createCellBg(true)
        }

        updateSelectionDisplay()
        checkSum()
    }

    private fun updateSelectionDisplay() {
        if (selectedIndices.isEmpty()) {
            selectionLabel.text = "Selected: —"
            return
        }
        val parts = selectedIndices.map { gridNumbers[it].toString() }
        val sum = selectedIndices.sumOf { gridNumbers[it] }
        selectionLabel.text = "Selected: ${parts.joinToString(" + ")} = $sum"
    }

    private fun checkSum() {
        val sum = selectedIndices.sumOf { gridNumbers[it] }

        if (sum == targetSum) {
            // Correct! Unlock
            Toast.makeText(context, "Touch Lock Disabled", Toast.LENGTH_SHORT).show()
            onUnlocked()
        } else if (sum > targetSum) {
            // Over — reset
            Toast.makeText(context, "Too high! Try again.", Toast.LENGTH_SHORT).show()
            resetSelection()
        }
    }

    private fun resetSelection() {
        for (i in selectedIndices) {
            gridButtons[i].background = createCellBg(false)
        }
        selectedIndices.clear()
        selectionLabel.text = "Selected: —"
    }
}
