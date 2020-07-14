package com.dailystudio.tflite.example.image.segmentation.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.ui.AbsSurfaceView
import com.dailystudio.devbricksx.utils.MatrixUtils.getTransformationMatrix
import org.tensorflow.lite.examples.imagesegmentation.ImageUtils

class MaskOverlay: AbsSurfaceView {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private var maskBitmap: Bitmap? = null

    private var frameToCanvasMatrix: Matrix? = null
    private var frameWidth = 0
    private var frameHeight = 0
    private var sensorOrientation = 0

    fun setMask(mask: Bitmap?) {
        this.maskBitmap = mask

//        requestLayout()
    }

    @Synchronized
    fun setFrameConfiguration(
        width: Int, height: Int, sensorOrientation: Int
    ) {
        this.frameWidth = width
        this.frameHeight = height
        this.sensorOrientation = sensorOrientation
    }

    override fun drawingCanvas(canvas: Canvas) {
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            strokeWidth = 2f
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        maskBitmap?.let {
            val matrix = getTransformationMatrix(
                it.width, it.height, canvasWidth, canvasHeight, 0,
                maintainAspectRatio = true,
                fitIn = false
            )

            val transformed = com.dailystudio.devbricksx.utils.ImageUtils.createTransformedBitmap(
                it, matrix!!)
            canvas.drawBitmap(transformed, 0f, 0f, paint)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Logger.debug("onlayout: $left, $top, $right, $bottom")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (maskBitmap == null) {
            return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        val wSpec = MeasureSpec.makeMeasureSpec(maskBitmap!!.width, MeasureSpec.EXACTLY)
        val hSpec = MeasureSpec.makeMeasureSpec(maskBitmap!!.height, MeasureSpec.EXACTLY)

        super.onMeasure(wSpec, hSpec)
    }


}