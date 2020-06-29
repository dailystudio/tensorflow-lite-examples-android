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
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import org.tensorflow.lite.examples.gesture.ImageClassifier
import org.tensorflow.lite.examples.gesture.ImageClassifierFloatInception
import java.lang.NumberFormatException

private class GestureAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<ImageInferenceInfo, List<ImageClassifier.Recognition>>(rotation, lensFacing) {

    companion object {
        private const val PRE_SCALE_WIDTH = 640
        private const val PRE_SCALE_HEIGHT = 480

        private const val INFERENCE_SIZE = 224

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val CROPPED_IMAGE_FILE = "cropped.png"
    }

    private var classifier: ImageClassifier? = null

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): List<ImageClassifier.Recognition>? {
        var results: List<ImageClassifier.Recognition>? = null

        if (classifier == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                classifier = ImageClassifierFloatInception(context)
            }

            Logger.debug("classifier created: $classifier")
        }

        classifier?.let { classifier ->
            val start = System.currentTimeMillis()

            dumpIntermediateBitmap(inferenceBitmap, CROPPED_IMAGE_FILE)

            val intermediates = SpannableStringBuilder()
            classifier.classifyFrame(inferenceBitmap, intermediates)
            val inferenceEnd = System.currentTimeMillis()

            results = generateResults(intermediates.toString())
            Logger.debug("results: ${results.toString().replace("%", "%%")}")
            val end = System.currentTimeMillis()

            info.inferenceTime = (inferenceEnd - start)
            info.analysisTime = (end - start)
        }

        return results
    }

    private fun generateResults(intermediates: String): List<ImageClassifier.Recognition>? {
        if (intermediates.isBlank()) {
            return null
        }

        val lines = StringUtils.linesFromString(intermediates)
        if (lines.isEmpty()) {
            return null
        }

        var results: MutableList<ImageClassifier.Recognition>? = null
        for (line in lines) {
            val parts = line.split(":")
            if (parts.size != 2) {
                continue
            }
            if (results == null) {
                results = mutableListOf<ImageClassifier.Recognition>()
            }

            val label = parts[0]
            val prob = try {
                parts[1].toFloat()
            } catch (e: NumberFormatException) {
                Logger.warn("parse prob from [${parts[1]}] failed: $e")
                continue
            }

            val recognition = ImageClassifier.Recognition(label, label, prob, null)
            results.add(recognition)
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
            val thumb = ThumbnailUtils.extractThumbnail(it, INFERENCE_SIZE, INFERENCE_SIZE)

            if (info.lensFacing == CameraSelector.LENS_FACING_FRONT) {
                ImageUtils.flipBitmap(thumb)
            } else {
                thumb
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
        return true
    }

}

class GestureCameraFragment : AbsExampleCameraFragment<ImageInferenceInfo, List<ImageClassifier.Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int)
            : AbsImageAnalyzer<ImageInferenceInfo, List<ImageClassifier.Recognition>> {
        return GestureAnalyzer(rotation, lensFacing)
    }

    override fun getDefaultCameraLens(): Int {
        return CameraSelector.LENS_FACING_FRONT
    }

}