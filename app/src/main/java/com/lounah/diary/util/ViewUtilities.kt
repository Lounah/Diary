package com.lounah.diary.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import com.lounah.diary.R
import java.lang.Math.abs
import java.lang.Math.round

class ViewUtilities private constructor() {
    companion object {
        fun dpToPx(dp: Int, context: Context): Int = Math.round(dp * (context.resources.displayMetrics.densityDpi / 160f))

        fun pxToDp(px: Int, context: Context) = Math.round(px * DisplayMetrics.DENSITY_DEFAULT.toDouble()) / context.resources.displayMetrics.xdpi

        fun spToPx(sp: Float, context: Context): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()
        }

        fun isMotionEventInRect(targetRect: Rect, motionEvent: MotionEvent) = motionEvent.x >= targetRect.left - 20f
                && motionEvent.x <= targetRect.right + 20f
                && motionEvent.y >= targetRect.top - 20f
                && motionEvent.y <= targetRect.bottom + 20f

        fun isMotionEventInRect(targetRect: RectF, motionEvent: MotionEvent) = motionEvent.x >= targetRect.left - 20f
                && motionEvent.x <= targetRect.right + 20f
                && motionEvent.y >= targetRect.top - 20f
                && motionEvent.y <= targetRect.bottom + 20f

        fun drawOnClickShape(canvas: Canvas, targetRect: Rect, context: Context, paint: Paint) {
            canvas.drawCircle(targetRect.exactCenterX(), targetRect.exactCenterY(), abs(targetRect.right - targetRect.left) / 2f + dpToPx(8, context), paint)
        }

        fun isInLandscape(context: Context) = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        fun drawTopRoundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float, canvas: Canvas, paint: Paint) {
            canvas.drawRoundRect(RectF(left, top, right, bottom), radius, radius, paint)
            canvas.drawRect(
                    left,
                    top + radius,
                    right,
                    bottom,
                    paint
            )
        }

        fun drawElevation(viewRect: RectF,
                          elevationHeight: Float,
                          canvas: Canvas, elevationPaint: Paint,
                          elevationGravity: Int,
                          context: Context) {
            when (elevationGravity) {
                Gravity.BOTTOM -> {
                    canvas.drawRect(
                            viewRect.left,
                            viewRect.top,
                            viewRect.right,
                            viewRect.bottom + elevationHeight,
                            elevationPaint.apply {
                                shader = LinearGradient(0f,
                                        viewRect.top,
                                        0f,
                                        viewRect.bottom + elevationHeight,
                                        ContextCompat.getColor(context, R.color.elevationColorGradientStart),
                                        ContextCompat.getColor(context, R.color.elevationColorGradientEnd),
                                        Shader.TileMode.CLAMP)
                                alpha = 255
                            })
                }
                Gravity.TOP -> {
                    canvas.drawRect(
                            viewRect.left,
                            viewRect.top - elevationHeight,
                            viewRect.right,
                            viewRect.bottom,
                            elevationPaint.apply {
                                shader = LinearGradient(0f,
                                        viewRect.top - elevationHeight,
                                        0f,
                                        viewRect.bottom,
                                        ContextCompat.getColor(context, R.color.elevationColorGradientStart),
                                        ContextCompat.getColor(context, R.color.elevationColorGradientEnd),
                                        Shader.TileMode.CLAMP)
                                alpha = 255
                            })
                }
            }
        }

        fun drawElevation(leftBounds: Float, topBounds: Float, rightBounds: Float, bottomBounds: Float,
                          elevationHeight: Float,
                          canvas: Canvas, elevationPaint: Paint,
                          elevationGravity: Int,
                          context: Context) {
            when (elevationGravity) {
                Gravity.BOTTOM -> {
                    canvas.drawRect(
                            leftBounds,
                            topBounds,
                            rightBounds,
                            bottomBounds + elevationHeight,
                            elevationPaint.apply {
                                shader = LinearGradient(0f,
                                        topBounds,
                                        0f,
                                        bottomBounds + elevationHeight,
                                        ContextCompat.getColor(context, R.color.elevationColorGradientStart),
                                        ContextCompat.getColor(context, R.color.elevationColorGradientEnd),
                                        Shader.TileMode.CLAMP)
                                alpha = 255
                            })
                }
                Gravity.TOP -> {
                    canvas.drawRect(
                            leftBounds,
                            topBounds - elevationHeight,
                            rightBounds,
                            bottomBounds,
                            elevationPaint.apply {
                                shader = LinearGradient(0f,
                                        topBounds - elevationHeight,
                                        0f,
                                        bottomBounds,
                                        ContextCompat.getColor(context, R.color.elevationColorGradientStart),
                                        ContextCompat.getColor(context, R.color.elevationColorGradientEnd),
                                        Shader.TileMode.CLAMP)
                                alpha = 255
                            })
                }
            }
        }

    }
}