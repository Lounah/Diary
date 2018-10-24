package com.lounah.diary.presentation.components.tags

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.lounah.diary.R
import com.lounah.diary.util.ViewUtilities
import kotlin.math.abs

class TagView(context: Context, attributeSet: AttributeSet?, defStyleRes: Int)
    : View(context, attributeSet, defStyleRes) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    val COLOR_BROWN = ContextCompat.getColor(context, R.color.colorTagBrown)
    val COLOR_GREEN = ContextCompat.getColor(context, R.color.colorTagGreen)
    val COLOR_PINK = ContextCompat.getColor(context, R.color.colorTagPink)
    val COLOR_RED = ContextCompat.getColor(context, R.color.colorTagRed)
    val COLOR_BLUE = ContextCompat.getColor(context, R.color.colorTagBlue)
    val COLOR_YELLOW = ContextCompat.getColor(context, R.color.colorTagYellow)
    val COLOR_PURPLE = ContextCompat.getColor(context, R.color.colorTagPurple)
    val COLOR_BLACK = ContextCompat.getColor(context, R.color.black)
    val COLOR_WHITE = ContextCompat.getColor(context, R.color.white)

    var text: String = ""
        set(newValue) {
            field = newValue
            measureText()
            invalidate()
        }

    var tagColorRes: Int = ContextCompat.getColor(context, R.color.colorTagBlue)
        set(newValue) {
            field = newValue
            backgroundPaint.color = field
            textColorRes = when (field) {
                COLOR_BROWN, COLOR_YELLOW, COLOR_PURPLE -> {
                    COLOR_BLACK
                }
                else -> {
                    COLOR_WHITE
                }
            }
            invalidate()
        }

    var textColorRes: Int = ContextCompat.getColor(context, R.color.white)
        set(newValue) {
            field = newValue
            tagTextPaint.color = field
            invalidate()
        }

    private val DEFAULT_TAG_COLOR_RES = ContextCompat.getColor(context, R.color.colorTagBlue)
    private val DEFAULT_TEXT_COLOR_RES = ContextCompat.getColor(context, R.color.white)
    private val DEFAULT_TEXT_SIZE = ViewUtilities.spToPx(18f, context).toFloat()
    private val DEFAULT_MARGIN_16_DP = ViewUtilities.dpToPx(16, context)
    private val DEFAULT_MARGIN_8_DP = ViewUtilities.dpToPx(8, context)

    private val tagTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_TEXT_COLOR_RES
        textSize = DEFAULT_TEXT_SIZE
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_TAG_COLOR_RES
    }

    private var textMeasuredWidth = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (textMeasuredWidth == 0f) {
            measureText()
        }

        val textMetrics = tagTextPaint.fontMetrics

        val desiredWidth = DEFAULT_MARGIN_16_DP * 2 + textMeasuredWidth.toInt()

        val desiredHeight = DEFAULT_MARGIN_8_DP * 2 + abs(textMetrics.ascent - textMetrics.descent).toInt()

        val measuredWidth = resolveSize(desiredWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                ViewUtilities.dpToPx(20, context).toFloat(),
                ViewUtilities.dpToPx(20, context).toFloat(),
                backgroundPaint)

        canvas.drawText(
                text,
                DEFAULT_MARGIN_16_DP.toFloat(),
                (height / 2 - ((tagTextPaint.descent() + tagTextPaint.ascent()) / 2)),
                tagTextPaint
        )
    }

    private fun measureText() {
        if (text.isNotEmpty()) {
            textMeasuredWidth = tagTextPaint.measureText(text)
        }
    }
}