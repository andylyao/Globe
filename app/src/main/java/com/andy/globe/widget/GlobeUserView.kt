package com.andy.globe.widget

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.andy.globe.R
import com.andy.globe.bean.UserBean
import com.andy.globe.util.DisplayUtils


class GlobeUserView constructor(context: Context, val userBean: UserBean, private val userClickListener: OnClickListener?) : AppCompatImageView(context) {

    var viewSize = 0
    private var imageSize = 0
    private lateinit var mInsidePaint: Paint
    private lateinit var mInsideShadowPaint: Paint

    init {
        this.adjustViewBounds = true
        this.scaleType = ScaleType.CENTER_CROP
        setBackgroundResource(R.drawable.img_playground_user_bg)
        this.setOnClickListener {
            userClickListener?.onClick(it)
        }
    }

    private fun initPaint() {
        mInsidePaint = Paint()
        mInsidePaint.color = Color.parseColor("#CECECE")
        mInsidePaint.strokeWidth = DisplayUtils.dp2px(context, 3f).toFloat()
        mInsidePaint.style = Paint.Style.STROKE
        mInsideShadowPaint = Paint()
        mInsideShadowPaint.strokeWidth = DisplayUtils.dp2px(context, 3f).toFloat()
        mInsideShadowPaint.style = Paint.Style.STROKE
        mInsideShadowPaint.color = Color.parseColor("#CECECE")
        val shadowRadius = DisplayUtils.dp2px(context, 4f).toFloat()
        mInsideShadowPaint.maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.OUTER)
        setLayerType(View.LAYER_TYPE_SOFTWARE,null)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewSize = w
        imageSize = (viewSize * 0.54).toInt()
        val padding = (viewSize - imageSize) / 2
        setPadding(padding, padding, padding, padding)
        initPaint()
    }

    fun getRatio(): Double {
        return userBean.showRatio
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val dotRadius = DisplayUtils.dp2px(context, 2f).toFloat()
        val extendOffset = mInsidePaint.strokeWidth + dotRadius
        var radiusSize = (viewSize - extendOffset * 2) / 2
        var centerPoint = radiusSize + extendOffset
        var insideRadiusSize = (imageSize / 2).toFloat()
        canvas?.drawCircle(centerPoint, centerPoint, insideRadiusSize, mInsidePaint)
        canvas?.drawCircle(centerPoint, centerPoint, insideRadiusSize, mInsideShadowPaint)
    }
}