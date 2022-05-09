package com.dailystudio.tflite.example.transfer

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.transfer.fragment.TransferLearningCameraFragment
import com.google.android.material.button.MaterialButton

class ExampleActivity : AbsExampleActivity<InferenceInfo, Void>() {

    companion object {
        const val NUM_OF_CLASSES = 4

    }
    private var classButtons = arrayOfNulls<View?>(NUM_OF_CLASSES)

    override fun setupViews() {
        super.setupViews()

        for (i in 0 until NUM_OF_CLASSES) {
            val viewId = resources.getIdentifier("class_${i}_btn",
                "id", packageName)
            classButtons[i] = findViewById(viewId)
            classButtons[i]?.setOnClickListener { view ->
                val sampleClass = dumpSampleClassFromId(view.id)

                Logger.debug("click on class: $sampleClass")

                sampleClass?.let {
                    (exampleFragment as? TransferLearningCameraFragment)?.addSamples(it)
                }
            }
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_model_personalization
    }

    override fun createBaseFragment(): Fragment {
        return TransferLearningCameraFragment()
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

    private fun dumpSampleClassFromId(id: Int): String? {
        for (i in 0 until NUM_OF_CLASSES) {
            val viewId = resources.getIdentifier("class_${i}_btn",
                "id", packageName)

            if (id == viewId) {
                return "${i + 1}"
            }
        }

        return null
    }
}
