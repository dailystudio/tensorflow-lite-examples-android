package com.dailystudio.tflite.example.image.classification.fragment

import android.graphics.Bitmap
import android.util.Size
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.utils.scaleAndCenterCrop
import com.dailystudio.tflite.example.common.utils.scaleBitmapWithRatio
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.examples.classification.tflite.Classifier.Recognition

private class ImageClassificationAnalyzer(rotation: Int) : AbsExampleAnalyzer<InferenceInfo, List<Recognition>>(rotation) {

    private var classifier: Classifier? = null

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: InferenceInfo): List<Recognition>? {
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

    override fun createInferenceInfo(): InferenceInfo {
       return InferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: InferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, 640, 480, 0, true)

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix)
    }

}

class ImageClassificationFragment : AbsExampleFragment<InferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int): AbsExampleAnalyzer<InferenceInfo, List<Recognition>> {
        return ImageClassificationAnalyzer(rotation)
    }

}