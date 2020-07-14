package com.dailystudio.tflite.example.image.segmentation.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.ui.AbsSurfaceView
import com.dailystudio.devbricksx.utils.MatrixUtils.getTransformationMatrix
import org.tensorflow.lite.examples.imagesegmentation.ImageUtils

class MaskOverlay: View {

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

    fun setMask(mask: Bitmap?) {
        this.maskBitmap = mask

        requestLayout()
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        val canvasWidth = canvas?.width ?: 0
        val canvasHeight = canvas?.height ?: 0

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            strokeWidth = 2f
        }

        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        maskBitmap?.let {
            val matrix = getTransformationMatrix(
                it.width, it.height, canvasWidth, canvasHeight, 0,
                maintainAspectRatio = true,
                fitIn = false
            )

            val transformed = com.dailystudio.devbricksx.utils.ImageUtils.createTransformedBitmap(
                it, matrix!!)
            canvas?.drawBitmap(transformed, 0f, 0f, paint)
        }
    }


}