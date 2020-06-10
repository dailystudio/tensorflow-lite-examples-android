package com.dailystudio.tflite.example.image.pose.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.utils.getRotatedCropMatrix
import org.tensorflow.lite.examples.posenet.lib.Device
import org.tensorflow.lite.examples.posenet.lib.Person
import org.tensorflow.lite.examples.posenet.lib.Posenet
import kotlin.math.roundToInt

class PoseAnalyzer(rotation: Int) : AbsExampleAnalyzer<InferenceInfo, Person>(rotation) {

    companion object {
        const val MODEL_WIDTH = 257
        const val MODEL_HEIGHT = 257
        private const val CROPPED_IMAGE_FILE = "cropped.png"
    }

    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null

    private var croppedBitmap: Bitmap

    private var poseNet: Posenet? = null

    init {
        croppedBitmap = Bitmap.createBitmap(
            MODEL_WIDTH, MODEL_HEIGHT, Bitmap.Config.ARGB_8888)
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: InferenceInfo): Person? {
        var results: Person? = null

        if (poseNet == null) {
            val context = GlobalContextWrapper.context

            context?.let {
                poseNet = Posenet(context, device = Device.GPU)
            }

            Logger.debug("posenet created: $poseNet")
        }

        poseNet?.let { classifier ->
            val start = System.currentTimeMillis()
            results = classifier.estimateSinglePose(inferenceBitmap)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)

            Logger.debug("raw results: ${results.toString().replace("%", "%%")}")
            results?.let {
                mapKeyPoints(it)
            }

        }

        return results
    }

    private fun mapKeyPoints(person: Person) {
        for (keyPoint in person.keyPoints) {
            var pts = floatArrayOf(
                keyPoint.position.x.toFloat(),
                keyPoint.position.y.toFloat())
            cropToFrameTransform?.mapPoints(pts)

            keyPoint.position.x = pts[0].roundToInt()
            keyPoint.position.y = pts[1].roundToInt()
        }
    }

    override fun preProcessImage(frameBitmap: Bitmap?, info: InferenceInfo): Bitmap? {
        val scaledBitmap = frameBitmap

        scaledBitmap?.let {
            val cropWidth = MODEL_WIDTH
            val cropHeight = MODEL_HEIGHT

            val matrix = ImageUtils.getRotatedCropMatrix(
                it.width,
                it.height,
                cropWidth,
                cropHeight,
                info.imageRotation
            )

            frameToCropTransform = matrix
            cropToFrameTransform = Matrix()
            matrix.invert(cropToFrameTransform)

            val canvas = Canvas(croppedBitmap)
            canvas.drawBitmap(it, matrix, null)

            dumpIntermediateBitmap(croppedBitmap, CROPPED_IMAGE_FILE)
        }

        return croppedBitmap
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

}

class PoseFragment : AbsExampleFragment<InferenceInfo, Person>() {

    override fun createAnalyzer(screenAspectRatio: Int,
                                rotation: Int): AbsExampleAnalyzer<InferenceInfo, Person> {
        return PoseAnalyzer(rotation)
    }

}