package com.dailystudio.tflite.example.text.classification

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo

class ExampleActivity : AbsExampleActivity<InferenceInfo, Void>() {

    override fun createBaseFragment(): Fragment {
        return Fragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createSettingsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: Void) {
    }

}