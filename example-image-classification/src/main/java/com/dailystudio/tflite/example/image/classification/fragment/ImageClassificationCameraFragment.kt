package com.dailystudio.tflite.example.image.classification.fragment

import android.graphics.Bitmap
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.examples.classification.tflite.Classifier.Recognition

private class ImageClassificationAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<ImageInferenceInfo, List<Recognition>>(rotation, lensFacing) {

    private var classifier: Classifier? = null

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): List<Recognition>? {
        var results: List<Recognition>? = null

        if (classifier == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                classifier = Classifier.create(it,
                    Classifier.Model.QUANTIZED_EFFICIENTNET,
                    Classifier.Device.CPU,
                    1)
            }

            Logger.debug("classifier created: $classifier")
        }

        classifier?.let { classifier ->
            val start = System.currentTimeMillis()
            results = classifier.recognizeImage(inferenceBitmap, info.imageRotation)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
        }

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
       return ImageInferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, 640, 480, 0, true)

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix)
    }

}

class ImageClassificationCameraFragment : AbsExampleCameraFragment<ImageInferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int): AbsImageAnalyzer<ImageInferenceInfo, List<Recognition>> {
        return ImageClassificationAnalyzer(rotation, lensFacing)
    }

}