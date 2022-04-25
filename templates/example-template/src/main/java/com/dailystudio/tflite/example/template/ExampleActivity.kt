package com.dailystudio.tflite.example.template

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

    override fun onResultsUpdated(results: Void) {
    }

    override fun onInferenceInfoUpdated(info: InferenceInfo) {
        super.onInferenceInfoUpdated(info)
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
