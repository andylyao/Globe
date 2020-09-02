package com.andy.globe.widget

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.Scroller
import androidx.core.util.Consumer
import com.andy.globe.bean.UserBean
import com.andy.globe.util.DisplayUtils
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


class GlobeView constructor(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs)

    private val MAX_VELOCITY = 10000;
    private val MIN_VELOCITY = -10000;
    private var maxSize = 0
    private var minSize = 0
    private var commonOffset = 0
    private var scroller: Scroller = Scroller(context)
    private var velocityTracker: VelocityTracker? = null
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var deltaX: Float = 0f
    private var deltaY: Float = 0f
    private var childSize: Int = 0
    private var lastRequestScrollY = 0
    private var lastRequestScrollX = 0
    private var scrollLayout = false
    private var lastDownX: Float = 0f
    private var lastDownY: Float = 0f
    private val touchSlop : Int
    private val layoutViewList = arrayListOf<GlobeUserView>()
    private val unLayoutViewList = arrayListOf<GlobeUserView>()
    private val layoutXLength = 7
    private var layoutYLength = 0
    private var startUserRunnable: UserRunnable? = null
    private var userClickConsumer: Consumer<UserBean>? = null

    init {
        val vc = ViewConfiguration.get(context)
        touchSlop = vc.scaledTouchSlop

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        this.childSize = (width * 0.3).toInt()
        layoutYLength = height / childSize + if (height % childSize == 0) 4 else 5
        this.maxSize = (childSize * 0.8).toInt()
        this.minSize = (maxSize * 0.55).toInt()
        commonOffset = (childSize - maxSize) / 2
        for (position in 0 until childCount) {
            val child = getChildAt(position) as GlobeUserView
            val childRatioSize = (child.getRatio() * (maxSize - minSize)).toInt() + minSize
            child.measure(MeasureSpec.makeMeasureSpec(childRatioSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childRatioSize, MeasureSpec.EXACTLY))
        }
    }

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        super.addView(child, index, params)
        if (child != null && child is GlobeUserView) {
            unLayoutViewList.add(child)
        }
    }

    override fun removeAllViews() {
        super.removeAllViews()
        unLayoutViewList.clear()
        layoutViewList.clear()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (unLayoutViewList.isNullOrEmpty()) return
        val leftPosition = scrollX / childSize - 2
        val topPosition = scrollY / childSize - 2
        val layoutLeft = leftPosition * childSize
        val layoutTop = topPosition * childSize
        removeOutsideChild(layoutLeft, layoutTop)
        for (i in 0 until layoutYLength) {
            val topOffset = layoutTop + i * childSize
            for (j in 0 until layoutXLength) {
                if ((leftPosition + topPosition + i + j) % 2 == 0) continue
                var childTop = topOffset
                var childLeft = layoutLeft + childSize * j
                if (!layoutExits(childLeft, childTop)) {
                    if (unLayoutViewList.isNotEmpty()) {
                        val child = unLayoutViewList.removeAt(0)
                        layoutViewList.add(child)
                        val childRatioSize = (child.getRatio() * (maxSize - minSize)).toInt() + minSize
                        childLeft += ((maxSize - childRatioSize) * Math.random()).toInt()
                        childTop += ((maxSize - childRatioSize) * Math.random()).toInt()
                        childLeft += commonOffset
                        childTop += commonOffset
                        child.layout(childLeft, childTop, childLeft + childRatioSize, childTop + childRatioSize)
                    }
                }
            }

        }
    }

    private fun layoutExits(childLeft: Int, childTop: Int): Boolean {
        layoutViewList.forEach {
            if (it.left >= childLeft - childSize
                    && it.right <= childLeft + 2 * childSize
                    && it.top >= childTop
                    && it.bottom <= childTop + childSize) {
                return true
            }
        }
        return false
    }

    private fun removeOutsideChild(layoutLeft: Int, layoutTop: Int) {
        val layoutRight = layoutLeft + layoutXLength * childSize
        val layoutBottom = layoutTop + layoutYLength * childSize
        val iterator = layoutViewList.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            if (it.left >= layoutLeft
                    && it.right <= layoutRight
                    && it.top >= layoutTop
                    && it.bottom <= layoutBottom) {

            } else {
                iterator.remove()
                unLayoutViewList.add(it)
            }
        }
        unLayoutViewList.forEach {
            val childLeft = layoutLeft - childSize
            val childTop = layoutTop - childSize
            it.layout(childLeft, childTop, layoutLeft, layoutTop)
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            postInvalidate()
        }
        if (abs(scrollY - lastRequestScrollY) > childSize || abs(scrollX - lastRequestScrollX) > childSize) {
            scrollLayout = true
            lastRequestScrollY = scrollY
            lastRequestScrollX = scrollX
            requestLayout()
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        removeClickEvent()
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                lastDownX = event.x
                lastDownY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val xOffset = abs(event.x - lastDownX)
                val yOffset = abs(event.y - lastDownY)
                if(xOffset > touchSlop || yOffset > touchSlop) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        obtainVelocityTracker(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished)
                    scroller.abortAnimation()
            }
            MotionEvent.ACTION_MOVE -> {
                if (lastTouchY > 0 || lastTouchX > 0) {
                    deltaX = lastTouchX - event.x
                    deltaY = lastTouchY - event.y
                    scrollBy(deltaX.toInt(), deltaY.toInt())
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                lastTouchX = -1f
                lastTouchY = -1f
                velocityTracker?.computeCurrentVelocity(1000)
                val velocityX = calculateMaxMinVelocity(velocityTracker!!.xVelocity.toInt())
                val velocityY = calculateMaxMinVelocity(velocityTracker!!.yVelocity.toInt())
                if (!scroller.isFinished)
                    scroller.abortAnimation()
                val startX = scrollX
                val startY = scrollY
                scroller.fling(startX, startY, -velocityX, -velocityY, -Int.MAX_VALUE, Int.MAX_VALUE, -Int.MAX_VALUE, Int.MAX_VALUE)
                postInvalidate()
                releaseVelocityTracker()
            }
        }
        return true
    }

    private fun obtainVelocityTracker(event: MotionEvent?) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)
    }

    private fun releaseVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker?.clear()
            velocityTracker?.recycle()
            velocityTracker = null
        }
    }

    fun scrollToUserCenter(it: GlobeUserView) {
        val screenWidth = DisplayUtils.getScreenWidth(context) //获取屏幕宽度
        val screenHeight = DisplayUtils.getScreenHeight(context)
        val dx = scrollX + screenWidth / 2 - (it.left + it.right) / 2
        val dy = scrollY + screenHeight / 2 - (it.top + it.bottom) / 2
        if (!scroller.isFinished)
            scroller.abortAnimation()
        val maxMoveSize = (screenWidth / 2f - childSize / 2).pow(2) + (screenHeight / 2f - childSize / 2).pow(2)
        val currentMoveSize = dx.toDouble().pow(2) + dy.toDouble().pow(2)
        val moveRange = sqrt(currentMoveSize / maxMoveSize)
        val duration = (500 * moveRange).toInt() + 500
        scroller.startScroll(scrollX, scrollY, -dx, -dy, duration)
        postInvalidate()
        startUserRunnable = UserRunnable(userClickConsumer, it)
        this.postDelayed(startUserRunnable, duration.toLong() - 500)
    }

    fun removeClickEvent() {
        this.removeCallbacks(startUserRunnable)
    }

    private class UserRunnable(val consumer: Consumer<UserBean>?, val it: GlobeUserView) : Runnable {
        override fun run() {
            consumer?.accept(it.userBean)

        }
    }

    fun setUserClickConsumer(consumer: Consumer<UserBean>) {
        this.userClickConsumer = consumer
    }

    private fun calculateMaxMinVelocity(velocity: Int): Int {
        if (velocity > MAX_VELOCITY) return MAX_VELOCITY
        if (velocity < MIN_VELOCITY) return MIN_VELOCITY
        return velocity
    }
}