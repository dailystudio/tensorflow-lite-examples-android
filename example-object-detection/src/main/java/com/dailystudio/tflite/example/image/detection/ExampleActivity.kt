package com.dailystudio.tflite.example.image.detection

import android.view.View
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceInfoView
import com.dailystudio.tflite.example.image.detection.fragment.ObjectDetectionFragment
import org.tensorflow.lite.examples.detection.tflite.Classifier

class ExampleActivity : AbsExampleActivity<InferenceInfo, List<Classifier.Recognition>>() {

    private var inferenceInfoView: InferenceInfoView? = null

    override fun createExampleFragment(): AbsExampleFragment<InferenceInfo, List<Classifier.Recognition>> {
        return ObjectDetectionFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createHiddenView(): View? {
        inferenceInfoView = InferenceInfoView(this)

        return inferenceInfoView
    }

    override fun onResultsUpdated(result: List<Classifier.Recognition>) {
    }

    override fun onInferenceInfoUpdated(info: InferenceInfo) {
        inferenceInfoView?.setInferenceInfo(info)
    }

}
