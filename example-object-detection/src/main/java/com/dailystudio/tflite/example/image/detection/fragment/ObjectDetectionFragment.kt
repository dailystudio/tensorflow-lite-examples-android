package com.dailystudio.tflite.example.image.detection.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Environment
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.utils.getCropMatrix
import com.dailystudio.tflite.example.common.utils.getTransformationMatrix
import org.tensorflow.lite.examples.detection.tflite.Classifier
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel
import java.io.File

private class ObjectDetectionAnalyzer(rotation: Int) : AbsExampleAnalyzer<InferenceInfo, List<Classifier.Recognition>>(rotation) {

    companion object {
        private const val TF_OD_API_INPUT_SIZE = 300
        private const val TF_OD_API_IS_QUANTIZED = true
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"

        private const val TF_OD_FRAME_WIDTH = 640
        private const val TF_OD_FRAME_HEIGHT = 640

        private const val MAINTAIN_ASPECT = false
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f

        private const val DEBUG_FRAMES = false
        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val CROPPED_IMAGE_FILE = "cropped.png"
    }

    private var classifier: Classifier? = null

    private var preScaleTransform: Matrix? = null
    private var preScaleRevertTransform: Matrix? = null
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null

    private var croppedBitmap: Bitmap
    init {
        val cropSize = TF_OD_API_INPUT_SIZE

        croppedBitmap = Bitmap.createBitmap(
            cropSize, cropSize, Bitmap.Config.ARGB_8888)
    }


    override fun analyzeFrame(inferenceBitmap: Bitmap, info: InferenceInfo): List<Classifier.Recognition>? {
        var results: List<Classifier.Recognition>?

        if (classifier == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                classifier = TFLiteObjectDetectionAPIModel.create(
                    context.assets,
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED
                )
            }

            Logger.debug("classifier created: $classifier")
        }

        var mappedResults: List<Classifier.Recognition>? = null
        classifier?.let { classifier ->
            val start = System.currentTimeMillis()
            results = classifier.recognizeImage(inferenceBitmap)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)

            Logger.debug("raw results: ${results.toString().replace("%", "%%")}")
            results?.let {
                mappedResults = mapRecognitions(it)
            }

        }

        return mappedResults
    }

    private fun mapRecognitions(results: List<Classifier.Recognition>): List<Classifier.Recognition> {
        val mappedRecognitions: MutableList<Classifier.Recognition> =
            mutableListOf()

        for (result in results) {
            val location = result.location
            if (location != null && result.confidence >= MINIMUM_CONFIDENCE_TF_OD_API) {
                cropToFrameTransform?.mapRect(location)
                preScaleRevertTransform?.mapRect(location)

                result.location = location
                mappedRecognitions.add(result)
            }
        }

        return mappedRecognitions
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: InferenceInfo): Bitmap? {
        val scaledBitmap = preScaleImage(frameBitmap)

        scaledBitmap?.let {
            val cropSize = TF_OD_API_INPUT_SIZE

            val matrix = ImageUtils.getTransformationMatrix(
                it.width,
                it.height,
                cropSize,
                cropSize,
                info.imageRotation,
                MAINTAIN_ASPECT
            )

            frameToCropTransform = matrix
            cropToFrameTransform = Matrix()
            matrix.invert(cropToFrameTransform)

            val canvas = Canvas(croppedBitmap)
            canvas.drawBitmap(it, matrix, null)

            if (DEBUG_FRAMES) {
                val dir = GlobalContextWrapper.context?.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES
                )

                ImageUtils.saveBitmap(croppedBitmap, File(dir, CROPPED_IMAGE_FILE))
            }
        }

        return croppedBitmap
    }

    private fun preScaleImage(frameBitmap: Bitmap?): Bitmap? {
        if (frameBitmap == null) {
            return null
        }

        val matrix = ImageUtils.getCropMatrix(
            frameBitmap.width, frameBitmap.height,
            TF_OD_FRAME_WIDTH, TF_OD_FRAME_HEIGHT)

        preScaleTransform = matrix
        preScaleRevertTransform = Matrix()
        matrix.invert(preScaleRevertTransform)

       val scaledBitmap = if (frameBitmap.width > frameBitmap.height) {
            Bitmap.createBitmap(TF_OD_FRAME_WIDTH, TF_OD_FRAME_HEIGHT,
                Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(TF_OD_FRAME_HEIGHT, TF_OD_FRAME_WIDTH,
                Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(scaledBitmap)
        canvas.drawBitmap(frameBitmap, matrix, null)

        if (DEBUG_FRAMES) {
            val dir = GlobalContextWrapper.context?.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES)

            ImageUtils.saveBitmap(scaledBitmap, File(dir,
                PRE_SCALED_IMAGE_FILE))
        }

        return scaledBitmap
    }

}

class ObjectDetectionFragment : AbsExampleFragment<InferenceInfo, List<Classifier.Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int)
            : AbsExampleAnalyzer<InferenceInfo, List<Classifier.Recognition>> {
        return ObjectDetectionAnalyzer(rotation)
    }

}