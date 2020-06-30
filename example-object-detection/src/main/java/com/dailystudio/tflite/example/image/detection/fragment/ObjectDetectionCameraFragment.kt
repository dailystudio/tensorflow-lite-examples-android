package com.dailystudio.tflite.example.image.detection.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import org.tensorflow.lite.examples.detection.tflite.Classifier
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel

private class ObjectDetectionAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<ImageInferenceInfo, List<Classifier.Recognition>>(rotation, lensFacing) {

    companion object {
        private const val TF_OD_API_INPUT_SIZE = 300
        private const val TF_OD_API_IS_QUANTIZED = true
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"

        private const val TF_OD_FRAME_WIDTH = 640
        private const val TF_OD_FRAME_HEIGHT = 480

        private const val MAINTAIN_ASPECT = false
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f

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


    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): List<Classifier.Recognition>? {
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

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
        val scaledBitmap = preScaleImage(frameBitmap)

        scaledBitmap?.let {
            val cropSize = TF_OD_API_INPUT_SIZE

            val matrix = MatrixUtils.getTransformationMatrix(
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

            dumpIntermediateBitmap(croppedBitmap,  CROPPED_IMAGE_FILE)
        }

        return croppedBitmap
    }

    private fun preScaleImage(frameBitmap: Bitmap?): Bitmap? {
        if (frameBitmap == null) {
            return null
        }

        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width, frameBitmap.height,
            TF_OD_FRAME_WIDTH, TF_OD_FRAME_HEIGHT, 0, true)

        preScaleRevertTransform = Matrix()
        matrix.invert(preScaleRevertTransform)

       val scaledBitmap = ImageUtils.createTransformedBitmap(frameBitmap, matrix)

        dumpIntermediateBitmap(scaledBitmap,  PRE_SCALED_IMAGE_FILE)

        return scaledBitmap
    }

}

class ObjectDetectionCameraFragment : AbsExampleCameraFragment<ImageInferenceInfo, List<Classifier.Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int)
            : AbsImageAnalyzer<ImageInferenceInfo, List<Classifier.Recognition>> {
        return ObjectDetectionAnalyzer(rotation, lensFacing)
    }

}