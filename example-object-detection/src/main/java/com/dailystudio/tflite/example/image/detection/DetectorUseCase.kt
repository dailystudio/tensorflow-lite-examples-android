package com.dailystudio.tflite.example.image.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.examples.detection.tflite.Detector
import org.tensorflow.lite.examples.detection.tflite.ObjectDetectionModel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.fragment.ImageLiteUseCase
import org.tensorflow.litex.images.Recognition

class DetectorUseCase: ImageLiteUseCase<List<Recognition>, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "detector"

        private const val TF_OD_FRAME_WIDTH = 640
        private const val TF_OD_FRAME_HEIGHT = 480

        private const val MAINTAIN_ASPECT = false
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val CROPPED_IMAGE_FILE = "cropped.png"
    }

    private var preScaleTransform: Matrix? = null
    private var preScaleRevertTransform: Matrix? = null
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null

    private var croppedBitmap: Bitmap
    init {
        val cropSize = ObjectDetectionModel.TF_OD_API_INPUT_SIZE

        croppedBitmap = Bitmap.createBitmap(
            cropSize, cropSize, Bitmap.Config.ARGB_8888)
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(ObjectDetectionModel(
            context,
            device,
            numOfThreads,
            useXNNPack
        ))
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
        val scaledBitmap = preScaleImage(frameBitmap)

        scaledBitmap?.let {
            val cropSize = ObjectDetectionModel.TF_OD_API_INPUT_SIZE

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

            dumpIntermediateBitmap(croppedBitmap, CROPPED_IMAGE_FILE)
        }

        return croppedBitmap
    }

    private fun preScaleImage(frameBitmap: Bitmap?): Bitmap? {
        if (frameBitmap == null) {
            return null
        }

        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width, frameBitmap.height,
            TF_OD_FRAME_WIDTH,
            TF_OD_FRAME_HEIGHT, 0, true)

        preScaleRevertTransform = Matrix()
        matrix.invert(preScaleRevertTransform)

        val scaledBitmap = ImageUtils.createTransformedBitmap(frameBitmap, matrix)

        dumpIntermediateBitmap(scaledBitmap, PRE_SCALED_IMAGE_FILE)

        return scaledBitmap
    }

    override fun analyzeFrame(
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Recognition>? {
        var mappedResults: List<Recognition>? = null

        val start = System.currentTimeMillis()
        val results: List<Recognition>? =
            (defaultModel as? Detector)?.recognizeImage(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        Logger.debug("raw results: ${results.toString().replace("%", "%%")}")
        results?.let {
            mappedResults = mapRecognitions(it)
        }

        return mappedResults
    }

    private fun mapRecognitions(results: List<Recognition>): List<Recognition> {
        val mappedRecognitions: MutableList<Recognition> =
            mutableListOf()

        for (result in results) {
            val location = result.location
            if (location != null && (result.confidence ?: 0f) >= MINIMUM_CONFIDENCE_TF_OD_API) {
                cropToFrameTransform?.mapRect(location)
                preScaleRevertTransform?.mapRect(location)

                result.location = location
                mappedRecognitions.add(result)
            }
        }

        return mappedRecognitions
    }
}