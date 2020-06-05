package com.dailystudio.tflite.example.image.classification.fragment

import android.graphics.Bitmap
import android.util.Size
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.examples.classification.tflite.Classifier.Recognition

private class ImageClassificationAnalyzer(rotation: Int) : AbsExampleAnalyzer<InferenceInfo, List<Recognition>>(rotation) {

    private var classifier: Classifier? = null

    override fun analyzeFrame(frameBitmap: Bitmap, info: InferenceInfo): List<Recognition>? {
        var results: List<Recognition>? = null

        if (classifier == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                classifier = Classifier.create(it,
                    Classifier.Model.QUANTIZED_MOBILENET,
                    Classifier.Device.CPU,
                    1)
            }

            Logger.debug("classifier created: $classifier")
        }

        classifier?.let { classifier ->
            results = classifier.recognizeImage(frameBitmap, info.imageRotation)
        }

        return results
    }

    override fun createInferenceInfo(): InferenceInfo {
       return InferenceInfo()
    }

    override fun getDesiredImageResolution(): Size? {
        return Size(640, 360)
    }

}

class ImageClassificationFragment : AbsExampleFragment<InferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int): AbsExampleAnalyzer<InferenceInfo, List<Recognition>> {
        return ImageClassificationAnalyzer(rotation)
    }

}