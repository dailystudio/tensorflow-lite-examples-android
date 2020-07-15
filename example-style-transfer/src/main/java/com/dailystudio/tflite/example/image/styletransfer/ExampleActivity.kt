package com.dailystudio.tflite.example.image.styletransfer

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo

class ExampleActivity: AbsExampleActivity<ImageInferenceInfo, Void>() {

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