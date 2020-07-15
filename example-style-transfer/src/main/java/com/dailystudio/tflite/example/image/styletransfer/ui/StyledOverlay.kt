package com.dailystudio.tflite.example.image.styletransfer.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.dailystudio.devbricksx.utils.MatrixUtils.getTransformationMatrix

class StyledOverlay: View {

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

    fun setStyledOverlay(mask: Bitmap?) {
        this.maskBitmap = mask

        requestLayout()
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val canvasWidth = width
        val canvasHeight = height

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        maskBitmap?.let {
            val matrix = getTransformationMatrix(
                it.width, it.height, canvasWidth, canvasHeight, 0,
                maintainAspectRatio = true,
                fitIn = false
            )

            val transformed = com.dailystudio.devbricksx.utils.ImageUtils.createTransformedBitmap(
                it, matrix)
            canvas.drawBitmap(transformed, 0f, 0f, paint)
        }
    }


}