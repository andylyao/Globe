package com.andy.globe.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.andy.globe.bean.UserBean
import com.andy.globe.util.DisplayUtils
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class GlobeUserBackgroundView constructor(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs)


    private var viewSize = 0
    private var circleMargin = 0f
    private var dotAngle = 0f
    private var userBean : UserBean? = null
    private lateinit var mCirclePaint: Paint
    private lateinit var mInsidePaint: Paint
    private lateinit var mInsideBackgroundPaint: Paint
    private lateinit var mDotPaint: Paint
    private var isFirstInvalidate = true

    private fun initPaint() {
        mCirclePaint = Paint()
        mCirclePaint.color = Color.parseColor("#7E7E7E")
        mCirclePaint.strokeWidth = 1f
        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.alpha = 90
        mInsidePaint = Paint()
        mInsidePaint.color = Color.parseColor("#CECECE")
        mInsidePaint.strokeWidth = 1f
        mInsidePaint.style = Paint.Style.STROKE
        mInsidePaint.alpha = 200
        mInsideBackgroundPaint = Paint()
        mInsideBackgroundPaint.color = Color.parseColor("#60000000")
        mInsideBackgroundPaint.style = Paint.Style.FILL
        mInsideBackgroundPaint.alpha = 100
        mDotPaint = Paint()
        mDotPaint.color = Color.parseColor("#FEBD02")
        mDotPaint.style = Paint.Style.FILL
        setLayerType(LAYER_TYPE_SOFTWARE,null)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewSize = w
        circleMargin = DisplayUtils.dp2px(context, 37f).toFloat()
        val padding = circleMargin.toInt() + 3
        setPadding(padding, padding, padding, padding)
        initPaint()
    }

    fun bindUserData(userBean: UserBean?) {
        this.userBean = userBean
        this.dotAngle = userBean?.dotAngle!!
        initPaint()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val extendOffset = mCirclePaint.strokeWidth
        var radiusSize = (viewSize - extendOffset * 2) / 2
        var insideRadiusSize = radiusSize - circleMargin
        var centerPoint = radiusSize + extendOffset
        canvas?.drawCircle(centerPoint, centerPoint, insideRadiusSize - mInsidePaint.strokeWidth, mInsideBackgroundPaint)
        canvas?.drawCircle(centerPoint, centerPoint, radiusSize, mCirclePaint)
        canvas?.drawCircle(centerPoint, centerPoint, insideRadiusSize, mInsidePaint)
        val dotCenterX = centerPoint - insideRadiusSize * cos(dotAngle / 90f)
        val dotCenterY = centerPoint + insideRadiusSize * sin(dotAngle / 90f)
        canvas?.drawCircle(dotCenterX, dotCenterY, 8f, mDotPaint)

        if(userBean != null && abs(userBean!!.dotAngle - dotAngle)  <= 540) {
            postDelayed(Runnable {
                dotAngle -= 36
                invalidate()
            }, if (isFirstInvalidate) 550 else 50)
        }
        isFirstInvalidate = false
    }
}