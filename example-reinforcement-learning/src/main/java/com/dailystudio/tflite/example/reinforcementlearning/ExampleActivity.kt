package com.dailystudio.tflite.example.reinforcementlearning

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.reinforcementlearning.fragment.ReinforcementLearningFragment

class ExampleActivity : AbsExampleActivity<InferenceInfo, Int>() {

    private var fabResetGame: View? = null

    override fun setupViews() {
        super.setupViews()

        fabResetGame = findViewById(R.id.fab_reset)
        fabResetGame?.setOnClickListener {
            val fragment = exampleFragment
            if (fragment is ReinforcementLearningFragment) {
                fragment.resetGame()
            }
        }

    }
    override fun createBaseFragment(): Fragment {
        return ReinforcementLearningFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_reinforcement_learning
    }

    override fun onResultsUpdated(results: Int) {
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
