package com.dailystudio.tflite.example.image.detection.fragment

import android.graphics.Bitmap
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import org.tensorflow.lite.examples.detection.tflite.Classifier
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel

private class ObjectDetectionAnalyzer(rotation: Int) : AbsExampleAnalyzer<InferenceInfo, List<Classifier.Recognition>>(rotation) {

    companion object {
        private const val TF_OD_API_INPUT_SIZE = 300
        private const val TF_OD_API_IS_QUANTIZED = true
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
    }

    private var classifier: Classifier? = null

    override fun analyzeFrame(frameBitmap: Bitmap, info: InferenceInfo): List<Classifier.Recognition>? {
        var results: List<Classifier.Recognition>? = null

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

        classifier?.let { classifier ->
            val start = System.currentTimeMillis()
            results = classifier.recognizeImage(frameBitmap)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
        }

        return results
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

}

class ObjectDetectionFragment : AbsExampleFragment<InferenceInfo, List<Classifier.Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int)
            : AbsExampleAnalyzer<InferenceInfo, List<Classifier.Recognition>> {
        return ObjectDetectionAnalyzer(rotation)
    }

}