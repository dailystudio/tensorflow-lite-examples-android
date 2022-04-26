package com.dailystudio.tflite.example.image.superresolution.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils.getTransformationMatrix
import com.dailystudio.tflite.example.image.superresolution.R

class ZoomOverlay: View {

    companion object {
        const val TARGET_IMAGE_WIDTH = 200
        const val TARGET_IMAGE_HEIGHT = 200
    }

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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var zoomedBitmap: Bitmap? = null

    fun setZoomedOverlay(mask: Bitmap?) {
        this.zoomedBitmap = mask

        setBackgroundColor(Color.BLACK)
        requestLayout()
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val canvasWidth = width
        val canvasHeight = height

        canvas.drawColor(Color.BLACK)

    }

}