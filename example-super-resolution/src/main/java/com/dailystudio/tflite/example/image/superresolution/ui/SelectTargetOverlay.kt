package com.dailystudio.tflite.example.image.superresolution.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.image.superresolution.R
import java.lang.reflect.Modifier

class SelectOverlay: View {

    companion object {
        const val SELECT_RADIUS = 25
    }

    private var scaleFactor: Float = 1f

    private var offlineBitmap = Bitmap.createBitmap(
        1, 1, Bitmap.Config.ARGB_8888
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(128, 0, 0, 0)
    }
    private val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
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

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawBitmap(offlineBitmap, 0f, 0f, paint)
//        drawSelector(canvas)
    }

    fun setScaleFactor(factor: Float) {
        Logger.debug("new scale factor: $factor")
        scaleFactor = factor

        invalidateOfflineBuffer()
        invalidate()
    }

    private fun invalidateOfflineBuffer() {
        offlineBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(offlineBitmap)
        drawSelector(canvas)
    }

    private fun drawSelector(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f

        val radius = SELECT_RADIUS * scaleFactor

        val background = RectF(
            0f, 0f,
            width.toFloat(), height.toFloat()
        )

        val selected = RectF(
            cx - radius, cy - radius,
            cx + radius, cy + radius
        )

        val rectRadius = resources.getDimensionPixelSize(
            R.dimen.selection_rect_round_corner_radius).toFloat()

        canvas.drawRect(background, backgroundPaint)
        canvas.drawRoundRect(selected, rectRadius, rectRadius, clipPaint)
    }

}