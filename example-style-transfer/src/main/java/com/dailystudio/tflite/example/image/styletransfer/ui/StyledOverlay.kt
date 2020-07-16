package com.dailystudio.tflite.example.image.styletransfer.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils.getTransformationMatrix
import com.dailystudio.tflite.example.image.styletransfer.R

class StyledOverlay: View {

    companion object {
        const val TARGET_IMAGE_WIDTH = 512
        const val TARGET_IMAGE_HEIGHT = 512
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
                TARGET_IMAGE_WIDTH
            } else {
                canvasWidth
            }

            val destHeight: Int = if (inPreviewMode) {
                TARGET_IMAGE_HEIGHT
            } else {
                canvasHeight
            }

            val matrix = getTransformationMatrix(
                it.width, it.height, destWidth, destHeight, 0,
                maintainAspectRatio = true,
                fitIn = inPreviewMode
            )

            val transformed = ImageUtils.createTransformedBitmap(
                    it, matrix)

            val displayBitmap = if (inPreviewMode) {
                val trimmed = ImageUtils.trimBitmap(transformed,
                    it.width.toFloat() / it.height)
                val radius = resources.getDimensionPixelSize(R.dimen.preview_round_corner_radius)

                ImageUtils.clipBitmapWithRoundCorner(trimmed, radius.toFloat())
            } else {
                transformed
            }

            if (inPreviewMode) {
                val marginEnd = resources.getDimensionPixelSize(R.dimen.preview_end_margin)
                val marginBottom = resources.getDimensionPixelSize(R.dimen.preview_bottom_margin)
                canvas.drawBitmap(displayBitmap,
                    (canvasWidth - displayBitmap.width - marginEnd).toFloat(),
                    (canvasHeight - displayBitmap.height - marginBottom).toFloat(), paint)
            } else {
                canvas.drawBitmap(displayBitmap, 0f, 0f, paint)
            }

        }
    }

}