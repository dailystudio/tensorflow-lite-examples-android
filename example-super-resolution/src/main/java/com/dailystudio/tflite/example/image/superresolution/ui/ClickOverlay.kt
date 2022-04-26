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

data class ClickInfo(
    val visibleArea: RectF,
    val clickedArea: RectF,
)

class ClickOverlay: View {

    companion object {
        const val SELECT_RADIUS = 25
    }

    private var visibleArea: RectF = RectF()
    private var clickedArea: RectF = RectF()
    private val _selectedAreaLiveData = MutableLiveData(
        ClickInfo(visibleArea, clickedArea)
    )

    val selectedAreaLiveData: LiveData<ClickInfo> = _selectedAreaLiveData

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
        isClickable = true
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        visibleArea.set(
            left.toFloat(), top.toFloat(),
            right.toFloat(), bottom.toFloat()
        )

        updateClickInfo()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            clickedArea.set(
                event.x - SELECT_RADIUS, event.y - SELECT_RADIUS,
                event.x + SELECT_RADIUS, event.y + SELECT_RADIUS,
            )

            updateClickInfo()

            invalidate()
        }

        return super.onTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (clickedArea.isEmpty) {
            return
        }

        val canvasWidth = width
        val canvasHeight = height

        Logger.debug("[CLICK] canvas: w = $canvasWidth, h = $canvasHeight")
        Logger.debug("[CLICK] selected: $clickedArea")

        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
        canvas.drawRect(clickedArea, paint)
    }

    private fun updateClickInfo() {
        _selectedAreaLiveData.postValue(ClickInfo(visibleArea, clickedArea))
    }

}