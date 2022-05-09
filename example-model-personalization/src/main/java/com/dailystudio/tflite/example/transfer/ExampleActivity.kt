package com.dailystudio.tflite.example.transfer

import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.transfer.fragment.TransferLearningCameraFragment
import com.dailystudio.tflite.example.transfer.model.TransferLearningModel
import com.google.android.material.button.MaterialButton

class ExampleActivity : AbsExampleActivity<InferenceInfo, Array<TransferLearningModel.Prediction>>() {

    companion object {
        const val NUM_OF_CLASSES = 4

    }
    private var classButtons = arrayOfNulls<View?>(NUM_OF_CLASSES)
    private var trainButton: Button? = null

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
                    (exampleFragment as? TransferLearningCameraFragment)?.addSample(it)
                }
            }
        }

        trainButton = findViewById(R.id.train_toggle_button)
        trainButton?.setOnClickListener {
            (exampleFragment as TransferLearningCameraFragment).enableTraining(
                TransferLearningModel.LossConsumer { epoch, loss ->

                })
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

    override fun onResultsUpdated(results: Array<TransferLearningModel.Prediction>) {
        for(i in 0 until results.size) {
            Logger.debug("pre [$i]: ${results[i]}")
        }
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
