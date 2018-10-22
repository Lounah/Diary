package com.lounah.diary.presentation.uicomponents

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import com.lounah.diary.R
import com.lounah.diary.util.ViewUtilities
import java.lang.Math.abs

class BottomNavigationView(context: Context, attributeSet: AttributeSet?, defStyleRes: Int)
    : View(context, attributeSet, defStyleRes) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    interface OnNavigationIconClickListener {
        fun onMenuToggleClicked()
        fun onAddButtonClicked()
        fun onSearchIconClicked()
        fun onMoreIconClicked()
    }

    interface expandableBottomMenuStateListener {
        fun onMenuStateChanged(newState: MenuState)
    }

    enum class MenuState {
        COLLAPSED, EXPANDING, EXPANDED, COLLAPSING
    }

    interface OnMenuItemClickListener {
        fun onMenuItemSelected(menuItem: MenuItem)
    }

    data class MenuItem(
            val title: String,
            val iconRes: Int,
            val isHeader: Boolean = false,
            val hasDivider: Boolean = true
    )

    ///////////////////////////// VIEW BASE //////////////////////////////////////////
    var onClickListener: OnNavigationIconClickListener? = null

    var currentMenuState: MenuState = MenuState.COLLAPSED

    var menuItems: List<MenuItem> = emptyList()
        set(newValue) {
            field = newValue
            invalidate()
        }

    lateinit var currentlySelectedMenuItem: MenuItem

    ///////////////////////////// DEFAULT VALUES //////////////////////////////////////////
    private val DEFAULT_MARGIN_16DP = ViewUtilities.dpToPx(16, context)

    private val DEFAULT_BOTTOM_BAR_HEIGHT = ViewUtilities.dpToPx(60, context)
    private val DEFAULT_BOTTOM_BAR_BACKGROUND_COLOR = Color.WHITE
    private val DEFAULT_ADD_ACTION_BUTTON_COLOR = Color.BLACK
    private val DEFAULT_EXPANDED_MENU_SHADOW_COLOR = Color.GRAY
    private val DEFAULT_SHEET_MENU_ITEM_HEIGHT = ViewUtilities.dpToPx(48, context)


    ///////////////////////////// ICONS //////////////////////////////////////////
    private val menuNavigationIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_menu_navigation_24dp)
    private val searchNavigationIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_search_black_24dp)
    private val dotsNavigationIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dots_black_24dp)
    private val plusIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_add_white_24dp)

    ///////////////////////////// PAINTS //////////////////////////////////////////
    private val bottomViewBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_BOTTOM_BAR_BACKGROUND_COLOR
    }

    private val addActionButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_ADD_ACTION_BUTTON_COLOR
    }

    private val expandableBottomMenuPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_BOTTOM_BAR_BACKGROUND_COLOR
    }

    private val expandedMenuShadowPaint = Paint().apply {
        alpha = 20
        color = DEFAULT_EXPANDED_MENU_SHADOW_COLOR
    }

    private var viewElevationPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.grey)
        alpha = 75
    }
    ///////////////////////////// RECTS //////////////////////////////////////////
    private lateinit var addActionButtonRect: Rect
    private lateinit var menuActionButtonRect: Rect
    private lateinit var searchActionButtonRect: Rect


    ///////////////////////////// ANIMATORS //////////////////////////////////////////

    val menuAnimatorSet = AnimatorSet()

    private var shadowAlphaAnimator = ValueAnimator.ofInt(20, 175)
    private var shadowAnimatorLastAnimatedValue = 0

    private lateinit var menuAnimator: ValueAnimator
    private var menuAnimatorLastAnimatedValue = 0f

    ///////////////////////////// CURRENT VIEW INFO //////////////////////////////////////////
    private var expandableMenuCurrentViewY: Float = -1f
        set(newValue) {
            field = newValue
            invalidate()
        }

    private var addActionButtonWasPressed = false
    private var menuActionButtonWasPressed = false
    private var moreActionButtonWasPressed = false
    private var searchActionButtonWasPressed = false

    private var viewIsDirty = true

    init {

        shadowAlphaAnimator.addUpdateListener(ShadowAlphaAnimatorUpdateListener())
        shadowAlphaAnimator.addListener(ShadowAlphaAnimatorListener())

        setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    when {
                        ViewUtilities.isMotionEventInRect(menuActionButtonRect, event) -> {
                            menuActionButtonWasPressed = true
                            shadowAlphaAnimator.start()
                            menuAnimator.start()
                            return@setOnTouchListener true
                        }
                        ViewUtilities.isMotionEventInRect(addActionButtonRect, event) -> {
                            addActionButtonWasPressed = true

                            return@setOnTouchListener true
                        }
                        ViewUtilities.isMotionEventInRect(searchActionButtonRect, event) -> {
                            searchActionButtonWasPressed = true

                            return@setOnTouchListener true
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {

                }
                MotionEvent.ACTION_UP -> {
                    releaseActionButtonsPressedState()
                }
            }
            return@setOnTouchListener false
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (viewIsDirty) {
            initDefaultValues()
            initRects()
            viewIsDirty = false
        }

        drawViewElevation(canvas)
        drawViewBackground(canvas)
        drawIcons(canvas)

        if (currentMenuState != MenuState.COLLAPSED) {
            drawMenuShadow(canvas)
            drawExpandableMenu(canvas)
        }

    }

    private fun initDefaultValues() {
        if (expandableMenuCurrentViewY == -1f) {
            expandableMenuCurrentViewY = height.toFloat()
        }

        menuAnimator = ValueAnimator.ofFloat(0f, ViewUtilities.dpToPx(290, context).toFloat())
        menuAnimator.addUpdateListener(MenuAnimatorUpdateListener())

    }

    private fun initRects() {
        addActionButtonRect = Rect(width / 2 - ViewUtilities.dpToPx(16, context),
                height - DEFAULT_BOTTOM_BAR_HEIGHT - ViewUtilities.dpToPx(16, context),
                width / 2 + ViewUtilities.dpToPx(16, context),
                height - DEFAULT_BOTTOM_BAR_HEIGHT + ViewUtilities.dpToPx(16, context))

        menuActionButtonRect = Rect(
                DEFAULT_MARGIN_16DP,
                height - DEFAULT_MARGIN_16DP - ViewUtilities.dpToPx(24, context) - ViewUtilities.dpToPx(4, context),
                DEFAULT_MARGIN_16DP + ViewUtilities.dpToPx(24, context) + ViewUtilities.dpToPx(8, context),
                height - DEFAULT_MARGIN_16DP
        )

        searchActionButtonRect = Rect(
                width - DEFAULT_MARGIN_16DP - ViewUtilities.dpToPx(24, context),
                height - DEFAULT_MARGIN_16DP - ViewUtilities.dpToPx(24, context),
                width - DEFAULT_MARGIN_16DP,
                height - DEFAULT_MARGIN_16DP
        )

    }

    private fun drawViewBackground(canvas: Canvas) {
        canvas.drawRect(0f,
                height - DEFAULT_BOTTOM_BAR_HEIGHT.toFloat(),
                width.toFloat(),
                height.toFloat(),
                bottomViewBackgroundPaint)
    }

    private fun drawIcons(canvas: Canvas) {
        menuNavigationIconDrawable?.let {
            it.bounds = menuActionButtonRect
            it.draw(canvas)
        }
        searchNavigationIconDrawable?.let {
            it.bounds = searchActionButtonRect
            it.draw(canvas)
        }

        // Add Icon Background
        canvas.drawCircle(width / 2f,
                height - DEFAULT_BOTTOM_BAR_HEIGHT.toFloat(),
                ViewUtilities.dpToPx(28, context).toFloat(),
                addActionButtonPaint)

        plusIconDrawable?.let {
            it.bounds = addActionButtonRect
            it.draw(canvas)
        }

    }

    private fun drawViewElevation(canvas: Canvas) {
        val viewY = height - DEFAULT_BOTTOM_BAR_HEIGHT.toFloat()
        ViewUtilities.drawElevation(
                leftBounds = 0f,
                rightBounds = viewY,
                topBounds = width.toFloat(),
                bottomBounds = viewY,
                elevationHeight = ViewUtilities.dpToPx(4, context).toFloat(),
                canvas = canvas,
                elevationPaint = viewElevationPaint,
                elevationGravity = Gravity.TOP,
                context = context)
    }

    private fun drawMenuShadow(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height - DEFAULT_BOTTOM_BAR_HEIGHT.toFloat(), expandedMenuShadowPaint)
    }

    private fun drawExpandableMenu(canvas: Canvas) {
        ViewUtilities.drawTopRoundRect(left = 0f,
                top = expandableMenuCurrentViewY,
                right = width.toFloat(),
                bottom = height.toFloat(),
                radius = ViewUtilities.dpToPx(16, context).toFloat(),
                canvas = canvas,
                paint = expandableBottomMenuPaint)
    }

    private fun releaseActionButtonsPressedState() {
        if (menuActionButtonWasPressed) {
            menuActionButtonWasPressed = false
            invalidate()
        }
        if (addActionButtonWasPressed) {
            addActionButtonWasPressed = false
            invalidate()
        }
        if (moreActionButtonWasPressed) {
            moreActionButtonWasPressed = false
            invalidate()
        }
        if (searchActionButtonWasPressed) {
            searchActionButtonWasPressed = false
            invalidate()
        }
    }

    private inner class ShadowAlphaAnimatorUpdateListener : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val animatedValue = animation.animatedValue as Int
            val delta = abs(shadowAnimatorLastAnimatedValue - animatedValue)

            if (currentMenuState == MenuState.COLLAPSED || currentMenuState == MenuState.EXPANDING) {
                expandedMenuShadowPaint.alpha = delta
                invalidate()
            }
        }
    }
    private inner class ShadowAlphaAnimatorListener : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            currentMenuState = MenuState.EXPANDED
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
            currentMenuState = MenuState.EXPANDING
        }
    }
    private inner class MenuAnimatorUpdateListener : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val animatedValue = animation.animatedValue as Float
            val delta = abs(menuAnimatorLastAnimatedValue - animatedValue)
            menuAnimatorLastAnimatedValue = animatedValue

            expandableMenuCurrentViewY -= delta
        }
    }
    private inner class MenuAnimatorListener : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {

        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {

        }
    }
}