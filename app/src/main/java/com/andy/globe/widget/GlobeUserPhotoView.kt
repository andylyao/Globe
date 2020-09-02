package com.andy.globe.widget

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.andy.globe.bean.UserBean
import com.andy.globe.util.DisplayUtils


class GlobeUserPhotoView constructor(context: Context, attrs: AttributeSet?) : CircleImageView(context, attrs) {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs)

    private var viewSize = 0
    private var imageSize = 0
    private lateinit var mInsidePaint: Paint
    private lateinit var mInsideShadowPaint: Paint
    private var userBean : UserBean? = null

    init {
        this.scaleType = ScaleType.CENTER_CROP
    }

    private fun initPaint() {
        mInsidePaint = Paint()
        mInsidePaint.color = context.resources.getColor(android.R.color.transparent)
        mInsidePaint.strokeWidth = DisplayUtils.dp2px(context, 3f).toFloat()
        mInsidePaint.style = Paint.Style.STROKE
        mInsideShadowPaint = Paint()
        mInsideShadowPaint.strokeWidth = DisplayUtils.dp2px(context, 3f).toFloat()
        mInsideShadowPaint.style = Paint.Style.STROKE
        mInsideShadowPaint.color = context.resources.getColor(android.R.color.transparent)
        val shadowRadius = DisplayUtils.dp2px(context, 7f).toFloat()
        mInsideShadowPaint.maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.OUTER)
        setLayerType(View.LAYER_TYPE_SOFTWARE,null)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewSize = w
        imageSize = viewSize - DisplayUtils.dp2px(context, 20f)
        val padding = (viewSize - imageSize) / 2
        setPadding(padding, padding, padding, padding)
        initPaint()
    }


    fun bindUserData(userBean: UserBean?) {
        this.userBean = userBean
        initPaint()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val extendOffset = DisplayUtils.dp2px(context, 7f).toFloat()
        var radiusSize = (viewSize - extendOffset * 2) / 2
        var centerPoint = radiusSize + extendOffset
        var insideRadiusSize = (imageSize / 2).toFloat()
        canvas?.drawCircle(centerPoint, centerPoint, insideRadiusSize, mInsidePaint)
        canvas?.drawCircle(centerPoint, centerPoint, insideRadiusSize, mInsideShadowPaint)
    }
}