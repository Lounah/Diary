package com.lounah.diary.presentation.uicomponents

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.SoundEffectConstants
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

    enum class FloatingActionButtonState {
        NORMAL_MODE, IN_ACTION_MODE
    }


    fun hideFab() {
        fabHideAnimator.start()
    }

    fun showFab() {
        fabShowAnimator.start()
    }

    fun changeFabActionState() {
        if (currentFabActionMode == FloatingActionButtonState.NORMAL_MODE) {
            currentFabActionMode = FloatingActionButtonState.IN_ACTION_MODE
        } else if (currentFabActionMode == FloatingActionButtonState.IN_ACTION_MODE) {
            currentFabActionMode = FloatingActionButtonState.NORMAL_MODE
        }
    }

    fun hideBottomNavigationView() {
        if (currentMenuState == MenuState.EXPANDED) {
            shadowAlphaAnimator.start()
            menuAnimator.start()
        }
        hideFab()
        animate().translationY(DEFAULT_BOTTOM_BAR_HEIGHT.toFloat()).apply {
            duration = 150
        }.start()
    }

    fun showBottomNavigationView() {
        showFab()
        animate().translationY(0f).apply {
            duration = 150
        }.start()
    }

    ///////////////////////////// VIEW BASE //////////////////////////////////////////
    var onClickListener: OnNavigationIconClickListener? = null

    var currentMenuState: MenuState = MenuState.COLLAPSED

    private var currentFabActionMode: FloatingActionButtonState = FloatingActionButtonState.NORMAL_MODE
        set(newMode) {
            field = newMode
            when (newMode) {
                FloatingActionButtonState.NORMAL_MODE -> {
                    hideFab()
                    currentFabCenterX = width / 2f
                    addActionButtonRect.offset((width / 2 - (width - ViewUtilities.dpToPx(64, context))), 0)
                    showFab()
                }
                FloatingActionButtonState.IN_ACTION_MODE -> {
                    hideFab()
                    currentFabCenterX = width - ViewUtilities.dpToPx(64, context).toFloat()
                    addActionButtonRect.offset(-(width / 2 - (width - ViewUtilities.dpToPx(64, context))), 0)
                    showFab()
                }
            }
            invalidate()
        }

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
    private val DEFAULT_FAB_RADIUS = ViewUtilities.dpToPx(28, context)

    ///////////////////////////// ICONS //////////////////////////////////////////
    private val menuNavigationIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_menu_navigation_24dp)
    private val searchNavigationIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_search_black_24dp)
    private val dotsNavigationIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dots_black_24dp)
    private val plusIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_add_white_24dp)
    private val doneIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_done_white_24dp)

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

    private var fabShowAnimator = ValueAnimator.ofFloat(0f, DEFAULT_FAB_RADIUS.toFloat()).apply {
        duration = 100
    }

    private var fabHideAnimator = ValueAnimator.ofFloat(0f, DEFAULT_FAB_RADIUS.toFloat()).apply {
        duration = 100
    }

    ///////////////////////////// CURRENT VIEW INFO //////////////////////////////////////////
    private var expandableMenuCurrentViewY: Float = -1f
        set(newValue) {
            field = newValue
            invalidate()
        }

    private var currentFabRadius: Float = DEFAULT_FAB_RADIUS.toFloat()
        set(newValue) {
            field = newValue
            invalidate()
        }

    private var currentFabCenterX: Float = 0f

    private var addActionButtonWasPressed = false
    private var menuActionButtonWasPressed = false
    private var moreActionButtonWasPressed = false
    private var searchActionButtonWasPressed = false

    private var fabIsShown = true

    private var viewIsDirty = true

    init {

        shadowAlphaAnimator.addUpdateListener(ShadowAlphaAnimatorUpdateListener())
        shadowAlphaAnimator.addListener(ShadowAlphaAnimatorListener())

        fabShowAnimator.addListener(FabAnimatorListener())
        fabShowAnimator.addUpdateListener(FabShowAnimatorUpdateListener())

        fabHideAnimator.addListener(FabAnimatorListener())
        fabHideAnimator.addUpdateListener(FabHideAnimatorUpdateListener())

        setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    when {
                        ViewUtilities.isMotionEventInRect(menuActionButtonRect, event) -> {
                            playSoundEffect(SoundEffectConstants.CLICK)
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
                    if ((event.y < height - ViewUtilities.dpToPx(290, context))
                            && (currentMenuState == MenuState.EXPANDED)) {
                        shadowAlphaAnimator.start()
                        menuAnimator.start()
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

        if (currentFabCenterX == 0f) {
            currentFabCenterX = width / 2f
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
        canvas.drawCircle(currentFabCenterX,
                height - DEFAULT_BOTTOM_BAR_HEIGHT.toFloat(),
                currentFabRadius,
                addActionButtonPaint)

        if (currentFabActionMode == FloatingActionButtonState.NORMAL_MODE && fabIsShown) {
            plusIconDrawable?.let {
                it.bounds = addActionButtonRect
                it.draw(canvas)
            }
        } else if (currentFabActionMode == FloatingActionButtonState.IN_ACTION_MODE && fabIsShown){
            doneIconDrawable?.let {
                it.bounds = addActionButtonRect
                it.draw(canvas)
            }
        }

    }

    private fun drawViewElevation(canvas: Canvas) {
        val viewY = height - DEFAULT_BOTTOM_BAR_HEIGHT.toFloat()
        ViewUtilities.drawElevation(
                leftBounds = 0f,
                topBounds = viewY,
                rightBounds = width.toFloat(),
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
            } else {
                expandedMenuShadowPaint.alpha = 175 - delta
                invalidate()
            }
        }
    }

    private inner class ShadowAlphaAnimatorListener : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            if (currentMenuState == MenuState.COLLAPSING) {
                currentMenuState = MenuState.COLLAPSED
            } else if (currentMenuState == MenuState.EXPANDING) {
                currentMenuState = MenuState.EXPANDED
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
            if (currentMenuState == MenuState.COLLAPSED) {
                currentMenuState = MenuState.EXPANDING
            } else if (currentMenuState == MenuState.EXPANDED) {
                currentMenuState = MenuState.COLLAPSING
            }
        }
    }

    private inner class MenuAnimatorUpdateListener : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val animatedValue = animation.animatedValue as Float
            menuAnimatorLastAnimatedValue = animatedValue
            if (currentMenuState == MenuState.COLLAPSED || currentMenuState == MenuState.EXPANDING) {
                expandableMenuCurrentViewY = height - animatedValue
            } else if (currentMenuState == MenuState.COLLAPSING){
                expandableMenuCurrentViewY = height - (ViewUtilities.dpToPx(290, context) - animatedValue)
            }
        }
    }

    private inner class FabShowAnimatorUpdateListener : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val animatedValue = animation.animatedValue as Float
            currentFabRadius = animatedValue
        }
    }

    private inner class FabHideAnimatorUpdateListener : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val animatedValue = animation.animatedValue as Float
            currentFabRadius = DEFAULT_FAB_RADIUS - animatedValue
        }
    }

    private inner class FabAnimatorListener : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            fabIsShown = !fabIsShown
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {

        }
    }
}