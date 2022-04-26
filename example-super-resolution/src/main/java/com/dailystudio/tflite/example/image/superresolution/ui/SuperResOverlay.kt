package com.dailystudio.tflite.example.image.superresolution.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.image.superresolution.R

class SuperResOverlay: View {

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

    private var superResBitmap: Bitmap? = null

    fun setSuperResBitmap(superRes: Bitmap?) {
        this.superResBitmap = superRes

        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wSpec = MeasureSpec.makeMeasureSpec(
            TARGET_IMAGE_WIDTH,
            MeasureSpec.EXACTLY
        )
        val hSpec = MeasureSpec.makeMeasureSpec(
            TARGET_IMAGE_HEIGHT,
            MeasureSpec.EXACTLY
        )
        super.onMeasure(wSpec, hSpec)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        Logger.debug("super res bitmap: $superResBitmap")

        superResBitmap?.let { bitmap ->

            val roundBitmap =
                ImageUtils.scaleBitmapRatioLocked(bitmap, TARGET_IMAGE_WIDTH, TARGET_IMAGE_WIDTH)

            canvas.drawBitmap(roundBitmap,
                0f, 0f, paint)
        }

    }

}