package com.dailystudio.tflite.example.image.pose.fragment

import android.content.Context
import android.graphics.*
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.pose.utils.mapKeyPoint
import org.tensorflow.lite.examples.posenet.lib.BodyPart
import org.tensorflow.lite.examples.posenet.lib.Person
import org.tensorflow.lite.examples.posenet.lib.Posenet
import org.tensorflow.lite.support.model.Model

class PoseAnalyzer(rotation: Int,
                   lensFacing: Int,
                   useAverageTime: Boolean)
    : AbsImageAnalyzer<Posenet, ImageInferenceInfo, Person>(rotation, lensFacing, useAverageTime) {

    companion object {
        const val MODEL_WIDTH = 257
        const val MODEL_HEIGHT = 257

        private const val MODEL_PATH = "posenet_model.tflite"
        private const val PRE_SCALE_WIDTH = 640
        private const val PRE_SCALE_HEIGHT = 480

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val CROPPED_IMAGE_FILE = "cropped.png"

        val BODY_JOINTS = listOf(
            Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
            Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
            Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
            Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
            Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
            Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
        )

        /** Threshold for confidence score. */
        const val MIN_CONFIDENCE = 0.5

        const val DEBUG_OUTPUTS = false
    }

    private var preScaleTransform: Matrix? = null
    private var preScaleRevertTransform: Matrix? = null

    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null

    private var originalBitmap: Bitmap? = null
    private var preScaledBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap = Bitmap.createBitmap(
        MODEL_WIDTH, MODEL_HEIGHT, Bitmap.Config.ARGB_8888)

    private var poseNet: Posenet? = null

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    @Synchronized
    override fun analyzeFrame(
        model: Posenet,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): Person? {
        val start = System.currentTimeMillis()
        val results = model.estimateSinglePose(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        Logger.debug("raw results: ${results.toString().replace("%", "%%")}")
        results?.let {
            debugOutputs(inferenceBitmap, it, "result.png")
            mapKeyPoints(it)
        }

        return results
    }

    private fun debugOutputs(bitmap: Bitmap, person: Person, filename: String) {
        if (!DEBUG_OUTPUTS) {
            return
        }

        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 1f

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        for (keyPoint in person.keyPoints) {
            if (keyPoint.score > MIN_CONFIDENCE) {
                canvas.drawCircle(
                    keyPoint.position.x.toFloat(),
                    keyPoint.position.y.toFloat(),
                    2f, paint)
            }
        }

        for (line in BODY_JOINTS) {
            if ((person.keyPoints[line.first.ordinal].score > MIN_CONFIDENCE)
                and (person.keyPoints[line.second.ordinal].score > MIN_CONFIDENCE)) {
                canvas.drawLine(
                    person.keyPoints[line.first.ordinal].position.x.toFloat(),
                    person.keyPoints[line.first.ordinal].position.y.toFloat(),
                    person.keyPoints[line.second.ordinal].position.x.toFloat(),
                    person.keyPoints[line.second.ordinal].position.y.toFloat(),
                    paint)
            }
        }

        saveIntermediateBitmap(output, filename)
    }

    private fun mapKeyPoints(person: Person) {
        for (keyPoint in person.keyPoints) {
            cropToFrameTransform?.mapKeyPoint(keyPoint)
        }

        preScaledBitmap?.let {
            debugOutputs(it, person, "pre-scaled.png")
        }

        for (keyPoint in person.keyPoints) {
            preScaleRevertTransform?.mapKeyPoint(keyPoint)
        }

        originalBitmap?.let {
            debugOutputs(it, person, "original.png")
        }
    }

    override fun preProcessImage(frameBitmap: Bitmap?, info: ImageInferenceInfo): Bitmap? {
        originalBitmap = frameBitmap
        preScaledBitmap = preScaleImage(frameBitmap)

        preScaledBitmap?.let {
            val cropWidth = MODEL_WIDTH
            val cropHeight = MODEL_HEIGHT

            val matrix = MatrixUtils.getTransformationMatrix(
                it.width,
                it.height,
                cropWidth,
                cropHeight,
                info.imageRotation,
                true
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

    private fun preScaleImage(frameBitmap: Bitmap?): Bitmap? {
        if (frameBitmap == null) {
            return null
        }

        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width, frameBitmap.height,
            PRE_SCALE_WIDTH, PRE_SCALE_HEIGHT, 0, true)

        preScaleTransform = matrix
        preScaleRevertTransform = Matrix()
        matrix.invert(preScaleRevertTransform)

        val scaledBitmap = ImageUtils.createTransformedBitmap(frameBitmap, matrix)

        dumpIntermediateBitmap(scaledBitmap,  PRE_SCALED_IMAGE_FILE)

        return scaledBitmap
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        settings: InferenceSettingsPrefs
    ): Posenet {
        return Posenet(context, MODEL_PATH, device, numOfThreads)
    }

}

class PoseCameraFragment : AbsExampleCameraFragment<Posenet, ImageInferenceInfo, Person>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean
    ): AbsImageAnalyzer<Posenet, ImageInferenceInfo, Person> {
        return PoseAnalyzer(rotation, lensFacing, useAverageTime)
    }

}