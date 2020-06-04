package com.dailystudio.tflite.example.image.classification.fragment

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.examples.classification.tflite.Classifier.Recognition

private class ImageClassificationAnalyzer(rotation: Int) : AbsExampleAnalyzer<List<Recognition>>(rotation) {

    private var classifier: Classifier? = null

    override fun analyzeFrame(frameBitmap: Bitmap, rotation: Int): List<Recognition>? {
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
            results = classifier.recognizeImage(frameBitmap, rotation)
        }

        return results
    }
}

class ImageClassificationFragment : AbsExampleFragment() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int): ImageAnalysis.Analyzer {
        return ImageClassificationAnalyzer(rotation)
    }

}