package com.dailystudio.tflite.example.image.detection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceInfoView
import com.dailystudio.tflite.example.image.detection.fragment.ObjectDetectionFragment
import kotlin.math.min

class ExampleActivity : AbsExampleActivity<InferenceInfo, Void>() {

    private var inferenceInfoView: InferenceInfoView? = null

    override fun createExampleFragment(): AbsExampleFragment<InferenceInfo, Void> {
        return ObjectDetectionFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createHiddenView(): View? {
        inferenceInfoView = InferenceInfoView(this)

        return inferenceInfoView
    }

    override fun onResultsUpdated(result: Void) {
    }

    override fun onInferenceInfoUpdated(info: InferenceInfo) {
        inferenceInfoView?.setInferenceInfo(info)
    }

}
