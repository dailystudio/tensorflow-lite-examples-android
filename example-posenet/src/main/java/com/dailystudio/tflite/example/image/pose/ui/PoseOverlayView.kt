package com.dailystudio.tflite.example.image.pose.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.image.pose.fragment.PoseAnalyzer
import com.dailystudio.tflite.example.image.pose.utils.mapPosition
import org.tensorflow.lite.examples.posenet.lib.Person

class PoseOverlayView: View {

    private var frameWidth = 0
    private var frameHeight = 0
    private var sensorOrientation = 0

    private var person: Person? = null

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

    fun setPersonPose(person: Person?) {
        this.person = person

        invalidate()
    }

    fun setFrameConfiguration(width: Int,
                              height: Int,
                              sensorOrientation: Int) {
        this.frameWidth = width
        this.frameHeight = height
        this.sensorOrientation = sensorOrientation
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val canvasWidth = canvas.width
        val canvasHeight = canvas.width

        val frameToCanvasMatrix = MatrixUtils.getTransformationMatrix(
            frameWidth,
            frameHeight,
            canvasWidth,
            canvasHeight,
            sensorOrientation,
            true
        )

        person?.let {
            val paint = Paint().apply {
                color = Color.RED
            }

            for (keyPoint in it.keyPoints) {
                Logger.debug("KP before: ${keyPoint.position}")
                frameToCanvasMatrix.mapPosition(keyPoint.position)
                Logger.debug("KP after: ${keyPoint.position}")
            }

            for (keyPoint in it.keyPoints) {
                if (keyPoint.score > PoseAnalyzer.MIN_CONFIDENCE) {
                    canvas.drawCircle(
                        keyPoint.position.x.toFloat(),
                        keyPoint.position.y.toFloat(),
                        10f, paint)
                }
            }

            for (line in PoseAnalyzer.BODY_JOINTS) {
                if ((it.keyPoints[line.first.ordinal].score > PoseAnalyzer.MIN_CONFIDENCE)
                    and (it.keyPoints[line.second.ordinal].score > PoseAnalyzer.MIN_CONFIDENCE)) {
                    canvas.drawLine(
                        it.keyPoints[line.first.ordinal].position.x.toFloat(),
                        it.keyPoints[line.first.ordinal].position.y.toFloat(),
                        it.keyPoints[line.second.ordinal].position.x.toFloat(),
                        it.keyPoints[line.second.ordinal].position.y.toFloat(),
                        paint)
                }
            }
        }
    }

}