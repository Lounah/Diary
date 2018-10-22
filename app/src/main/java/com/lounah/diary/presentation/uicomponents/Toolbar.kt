package com.lounah.diary.presentation.uicomponents

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.lounah.diary.util.ViewUtilities
import kotlin.math.abs
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import com.lounah.diary.R


class Toolbar(context: Context, attributeSet: AttributeSet?, defStyleRes: Int)
    : View(context, attributeSet, defStyleRes) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    interface OnNavigationIconClickListener {
        fun onNavigationIconClicked()
    }

    var navigationIconRes: Int = -1
        set(newIconRes) {
            field = newIconRes
            invalidate()
        }

    var menuIconRes: Int = -1
        set(newIconRes) {
            field = newIconRes
            invalidate()
        }

    var title: String = ""
        set(newTitle) {
            field = newTitle
            invalidate()
        }

    var titleGravity: Int = Gravity.CENTER
        set(newValue) {
            field = newValue
            invalidate()
        }

    var titleTextSize: Float = 0f
        set(newValue) {
            field = newValue
            titleTextPaint.textSize = newValue
            invalidate()
        }

    var titleTextColorRes: Int = -1
        set(newValue) {
            field = newValue
            invalidate()
        }

    var elevationHeight: Int = 0
        set(newValue) {
            field = newValue
            invalidate()
        }

    var shouldShowShadow = false
        set(newValue) {
            field = newValue
            invalidate()
        }

    private var DEFAULT_BACKGROUND_COLOR = Color.WHITE
    private var DEFAULT_TITLE_TEXT_COLOR = Color.BLACK
    private var DEFAULT_VIEW_HEIGHT_DP = ViewUtilities.dpToPx(56, context)
    private var MARGIN_16_DP = ViewUtilities.dpToPx(26, context)
    private var NAVIGATION_ICON_SIZE = ViewUtilities.dpToPx(24, context)
    private var DEFAULT_TITLE_TEXT_SIZE = ViewUtilities.spToPx(23f, context)

    private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_BACKGROUND_COLOR
    }

    private var titleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_TITLE_TEXT_COLOR
        textSize = DEFAULT_TITLE_TEXT_SIZE.toFloat()
        typeface = Typeface.create("sans-serif-light", Typeface.BOLD)
    }

    private var viewIsDirty = true

    private var navigationIconWasTouched = false
    private var menuIconWasTouched = false

    private var titleMeasuredWidth: Float = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val desiredWidth = width

        val desiredHeight = DEFAULT_VIEW_HEIGHT_DP

        val measuredWidth = resolveSize(desiredWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (viewIsDirty) {
            initDefaultValues()
            viewIsDirty = false
        }

        drawBackground(canvas)

        if (title.isNotEmpty()) {
            drawTitle(canvas)
        }
    }

    private fun initDefaultValues() {
        titleMeasuredWidth = titleTextPaint.measureText(title)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    private fun drawTitle(canvas: Canvas) {
        when (titleGravity) {
            Gravity.START -> {
                canvas.drawText(title, MARGIN_16_DP + NAVIGATION_ICON_SIZE + MARGIN_16_DP.toFloat(), height / 2f + 20f, titleTextPaint)
            }
            Gravity.CENTER -> {
                canvas.drawText(title, width / 2f - titleMeasuredWidth / 2, height / 2f + 20f, titleTextPaint)
            }
        }
    }
}