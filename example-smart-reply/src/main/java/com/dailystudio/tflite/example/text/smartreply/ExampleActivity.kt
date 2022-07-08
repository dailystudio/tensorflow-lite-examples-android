package com.dailystudio.tflite.example.text.smartreply

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity

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

    override fun getExampleThumbVideoResource(): Int {
        return R.raw.smart_reply_720p
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
