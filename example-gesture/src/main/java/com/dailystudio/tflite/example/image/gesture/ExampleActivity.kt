package com.dailystudio.tflite.example.image.gesture

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.image.gesture.fragment.GestureCameraFragment

class ExampleActivity : AbsExampleActivity<InferenceInfo, List<Void>>() {

    companion object {
    }

    override fun createBaseFragment(): Fragment {
        return GestureCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }


    override fun createSettingsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: List<Void>) {
    }

}
