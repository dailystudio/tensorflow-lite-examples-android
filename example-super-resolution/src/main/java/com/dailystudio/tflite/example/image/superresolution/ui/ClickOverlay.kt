package com.dailystudio.tflite.example.image.superresolution.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.ui.AbsSurfaceView
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils.getTransformationMatrix
import com.dailystudio.tflite.example.image.superresolution.R

class ClickOverlay: View {
    companion object {
        const val SELECT_RADIUS = 25
    }

    private var selectArea: RectF = RectF()

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

    init {
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            selectArea = RectF(
                event.x - SELECT_RADIUS,
                event.y - SELECT_RADIUS,
                event.x + SELECT_RADIUS,
                event.y + SELECT_RADIUS,
            )

            invalidate()
        }

        return super.onTouchEvent(event)
    }
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (selectArea.isEmpty) {
            return
        }
        val canvasWidth = width
        val canvasHeight = height

        Logger.debug("[CLICK] canvas: w = $canvasWidth, h = $canvasHeight")
        Logger.debug("[CLICK] selected: $selectArea")

        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
        canvas.drawRect(selectArea, paint)
    }

}