package com.dailystudio.tflite.example.template

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tensorflow.litex.InferenceInfo
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity

class ExampleActivity : LiteUseCaseActivity() {

    override fun createBaseFragment(): Fragment {
        return Fragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
    }

    override fun onInferenceInfoUpdated(nameOfUseCase: String, info: InferenceInfo) {
        super.onInferenceInfoUpdated(nameOfUseCase, info)
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

    override fun getExampleThumbVideoResource(): Int {

        return R.raw.about_video
    }

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf()
    }

}
