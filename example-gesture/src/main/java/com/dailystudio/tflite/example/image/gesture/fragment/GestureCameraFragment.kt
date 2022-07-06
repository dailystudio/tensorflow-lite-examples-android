package com.dailystudio.tflite.example.image.gesture.fragment

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import org.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.examples.gesture.Classifier
import org.tensorflow.lite.support.model.Model

private class GestureAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<Classifier, ImageInferenceInfo, List<Classifier.Recognition>>(rotation, lensFacing) {

    companion object {
        private const val PRE_SCALE_WIDTH = 640
        private const val PRE_SCALE_HEIGHT = 480

        private const val INFERENCE_SIZE = 224

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val CROPPED_IMAGE_FILE = "cropped.png"
    }

    private var classifier: Classifier? = null

    override fun analyzeFrame(
        model: Classifier,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Classifier.Recognition>? {
        var results: List<Classifier.Recognition>? = null

        classifier?.let { classifier ->
            val start = System.currentTimeMillis()

            dumpIntermediateBitmap(inferenceBitmap,
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

    override fun preProcessImage(frameBitmap: Bitmap?,
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


    private fun preScaleImage(frameBitmap: Bitmap?,
                              info: ImageInferenceInfo
    ): Bitmap? {
        if (frameBitmap == null) {
            return null
        }

        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width, frameBitmap.height,
            PRE_SCALE_WIDTH,
            PRE_SCALE_HEIGHT, info.imageRotation,
            true)

        val preScaledImage = ImageUtils.createTransformedBitmap(frameBitmap, matrix)

        dumpIntermediateBitmap(preScaledImage,
            PRE_SCALED_IMAGE_FILE
        )

        return preScaledImage
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Classifier? {
        return Classifier.create(context,
            Classifier.Model.FLOAT_INCEPTION,
            device,
            numOfThreads,
            useXNNPack
        )
    }

}

class GestureCameraFragment : AbsExampleCameraFragment<Classifier, ImageInferenceInfo, List<Classifier.Recognition>>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean
    ): AbsImageAnalyzer<Classifier, ImageInferenceInfo, List<Classifier.Recognition>> {
        return GestureAnalyzer(
            rotation,
            lensFacing
        )
    }

    override fun getDefaultCameraLens(): Int {
        return CameraSelector.LENS_FACING_FRONT
    }

}