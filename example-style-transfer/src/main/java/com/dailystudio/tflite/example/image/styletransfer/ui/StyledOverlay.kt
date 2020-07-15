package com.dailystudio.tflite.example.image.styletransfer.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.dailystudio.devbricksx.utils.MatrixUtils.getTransformationMatrix
import kotlin.math.roundToInt

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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var maskBitmap: Bitmap? = null
    private var inPreviewMode: Boolean = true

    fun setStyledOverlay(mask: Bitmap?) {
        this.maskBitmap = mask

        requestLayout()
        invalidate()
    }

    fun setPreviewMode(previewMode: Boolean) {
        inPreviewMode = previewMode

        invalidate()
    }

    fun isInPreviewMode(): Boolean {
        return inPreviewMode
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val canvasWidth = width
        val canvasHeight = height

        if (inPreviewMode) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }

        maskBitmap?.let {
            val destWidth: Int = if (inPreviewMode) {
                (canvasWidth / 3.0).roundToInt()
            } else {
                canvasWidth
            }

            val destHeight: Int = if (inPreviewMode) {
                (canvasHeight / 3.0).roundToInt()
            } else {
                canvasHeight
            }

            val matrix = getTransformationMatrix(
                it.width, it.height, destWidth, destHeight, 0,
                maintainAspectRatio = true,
                fitIn = false
            )

            val transformed =
                com.dailystudio.devbricksx.utils.ImageUtils.createTransformedBitmap(
                    it, matrix)
            canvas.drawBitmap(transformed, 0f, 0f, paint)
        }
    }


}