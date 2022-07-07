package com.dailystudio.tflite.example.template

import android.view.View
import androidx.fragment.app.Fragment
import org.tensorflow.litex.InferenceInfo
import com.dailystudio.tflite.example.template.fragment.ExampleTemplateCameraFragment
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.activity.LiteUseCaseActivity

class ExampleActivity : LiteUseCaseActivity() {

    override fun createBaseFragment(): Fragment {
        return ExampleTemplateCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
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

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            ExampleTemplateUseCase.UC_NAME to ExampleTemplateUseCase()
        )
    }

}
