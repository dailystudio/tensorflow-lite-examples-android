package com.dailystudio.tflite.example.text.smartreply

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.text.AbsChatActivity
import org.tensorflow.lite.examples.smartreply.SmartReply
import org.tensorflow.lite.examples.smartreply.SmartReplyClient
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.activity.LiteUseCaseActivity

class ExampleActivity : LiteUseCaseActivity() {

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
            SmartReplyUseCase.UC_NAME to SmartReplyUseCase()
        )
    }

    override fun createBaseFragment(): Fragment {
        return SmartReplyFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
    }

}
