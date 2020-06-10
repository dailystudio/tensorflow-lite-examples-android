package com.dailystudio.tflite.example.image.pose

import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceInfoView
import com.dailystudio.tflite.example.image.pose.fragment.PoseFragment
import org.tensorflow.lite.examples.posenet.lib.Person

class ExampleActivity : AbsExampleActivity<InferenceInfo, Person>() {

    private var inferenceInfoView: InferenceInfoView? = null

    override fun createExampleFragment(): AbsExampleFragment<InferenceInfo, Person> {
        return PoseFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createHiddenView(): View? {
        inferenceInfoView = InferenceInfoView(this)

        return inferenceInfoView
    }

    override fun onResultsUpdated(results: Person) {
        Logger.debug("detected pose: $results")
    }

    override fun onInferenceInfoUpdated(info: InferenceInfo) {
        inferenceInfoView?.setInferenceInfo(info)
    }

}