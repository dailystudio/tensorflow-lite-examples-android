package com.dailystudio.tflite.example.image.gesture

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import org.tensorflow.lite.examples.gesture.Classifier
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.fragment.ImageLiteUseCase
import org.tensorflow.litex.image.ImageInferenceInfo
import org.tensorflow.litex.ui.InferenceSettingsPrefs

class GestureUseCase: ImageLiteUseCase<List<Classifier.Recognition>, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "gesture"

        private const val PRE_SCALE_WIDTH = 640
        private const val PRE_SCALE_HEIGHT = 480

        private const val INFERENCE_SIZE = 224

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val CROPPED_IMAGE_FILE = "cropped.png"
    }

    override fun analyzeFrame(
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Classifier.Recognition>? {
        var results: List<Classifier.Recognition>? = null

        val classifier = defaultModel as? Classifier

        classifier?.let { classifier ->
            val start = System.currentTimeMillis()

            dumpIntermediateBitmap(
                inferenceBitmap,
                CROPPED_IMAGE_FILE
            )

            results = classifier.recognizeImage(inferenceBitmap, 0)
            val inferenceEnd = System.currentTimeMillis()

            Logger.debug("results: ${results.toString().replace("%", "%%")}")
            val end = System.currentTimeMillis()

            info.inferenceTime = (inferenceEnd - start)
            info.analysisTime = (end - start)
        }

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun preProcessImage(
        frameBitmap: Bitmap?,
        info: ImageInferenceInfo
    ): Bitmap? {
        val scaledBitmap = preScaleImage(frameBitmap, info)

        return scaledBitmap?.let {
            if (info.cameraLensFacing == CameraSelector.LENS_FACING_FRONT) {
                ImageUtils.flipBitmap(it)
            } else {
                it
            }
        }
    }

    private fun preScaleImage(
        frameBitmap: Bitmap?,
        info: ImageInferenceInfo
    ): Bitmap? {
        if (frameBitmap == null) {
            return null
        }

        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width, frameBitmap.height,
            PRE_SCALE_WIDTH,
            PRE_SCALE_HEIGHT, info.imageRotation,
            true
        )

        val preScaledImage = ImageUtils.createTransformedBitmap(frameBitmap, matrix)

        dumpIntermediateBitmap(
            preScaledImage,
            PRE_SCALED_IMAGE_FILE
        )

        return preScaledImage
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            Classifier.create(
                context,
                Classifier.Model.FLOAT_INCEPTION,
                device,
                numOfThreads,
                useXNNPack
            )
        )
    }
}