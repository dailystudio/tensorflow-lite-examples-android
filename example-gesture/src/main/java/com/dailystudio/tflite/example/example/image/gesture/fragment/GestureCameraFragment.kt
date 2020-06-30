package com.dailystudio.tflite.example.example.image.gesture.fragment

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.text.SpannableStringBuilder
import androidx.camera.core.CameraSelector
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.devbricksx.utils.StringUtils
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import org.tensorflow.lite.examples.gesture.Classifier

private class GestureAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<ImageInferenceInfo, List<Classifier.Recognition>>(rotation, lensFacing) {

    companion object {
        private const val PRE_SCALE_WIDTH = 640
        private const val PRE_SCALE_HEIGHT = 480

        private const val INFERENCE_SIZE = 224

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val CROPPED_IMAGE_FILE = "cropped.png"
    }

    private var classifier: Classifier? = null

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): List<Classifier.Recognition>? {
        var results: List<Classifier.Recognition>? = null

        if (classifier == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                classifier = Classifier.create(context,
                    Classifier.Model.FLOAT_INCEPTION,
                    Classifier.Device.GPU,
                    1
                )
            }

            Logger.debug("classifier created: $classifier")
        }

        classifier?.let { classifier ->
            val start = System.currentTimeMillis()

            dumpIntermediateBitmap(inferenceBitmap, CROPPED_IMAGE_FILE)

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
                                 info: ImageInferenceInfo): Bitmap? {
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
                              info: ImageInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return null
        }

        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width, frameBitmap.height,
            PRE_SCALE_WIDTH, PRE_SCALE_HEIGHT, info.imageRotation,
            true)

        val preScaledImage = ImageUtils.createTransformedBitmap(frameBitmap, matrix)

        dumpIntermediateBitmap(preScaledImage, PRE_SCALED_IMAGE_FILE)

        return preScaledImage
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

}

class GestureCameraFragment : AbsExampleCameraFragment<ImageInferenceInfo, List<Classifier.Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int)
            : AbsImageAnalyzer<ImageInferenceInfo, List<Classifier.Recognition>> {
        return GestureAnalyzer(rotation, lensFacing)
    }

    override fun getDefaultCameraLens(): Int {
        return CameraSelector.LENS_FACING_FRONT
    }

}