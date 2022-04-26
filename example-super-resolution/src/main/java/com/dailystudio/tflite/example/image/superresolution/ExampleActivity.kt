package com.dailystudio.tflite.example.image.superresolution

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.image.superresolution.fragment.SuperResolutionCameraFragment

class ExampleActivity : AbsExampleActivity<InferenceInfo, Void>() {

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_super_resolution
    }

    override fun createBaseFragment(): Fragment {
        return SuperResolutionCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: Void) {
    }

    override fun getExampleName(): CharSequence? {
        return getString(R.string.app_name)
    }

    override fun getExampleIconResource(): Int {
        return R.drawable.about_icon
    }

    override fun getExampleDesc(): CharSequence? {
        return getString(R.string.app_desc)
    }

}
