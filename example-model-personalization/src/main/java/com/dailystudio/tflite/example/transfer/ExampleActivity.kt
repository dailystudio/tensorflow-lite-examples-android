package com.dailystudio.tflite.example.transfer

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.transfer.fragment.TransferLearningCameraFragment
import com.dailystudio.tflite.example.transfer.fragment.TransferLearningViewModel
import com.dailystudio.tflite.example.transfer.model.TransferLearningModel
import com.google.android.material.button.MaterialButton

class ExampleActivity : AbsExampleActivity<InferenceInfo, Array<TransferLearningModel.Prediction>>() {

    companion object {
        const val NUM_OF_CLASSES = 4
    }

    private lateinit var viewModel: TransferLearningViewModel

    private var classButtons = arrayOfNulls<MaterialButton?>(NUM_OF_CLASSES)
    private var trainButton: Button? = null
    private var captureModelButton: RadioButton? = null
    private var inferenceModelButton: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[TransferLearningViewModel::class.java]

        viewModel.allClassTrainingInfosLive.observe(this) { classes ->
            attachClassTrainingInfo(classes)
        }

        viewModel.mode.observe(this) {
            when (it) {
                TransferLearningViewModel.Mode.Capture -> {
                    enableSamplesCollecting()

                    val classes = viewModel.getClassTrainingInfos()
                    attachClassTrainingInfo(classes)

                    trainButton?.visibility = View.VISIBLE
                }

                TransferLearningViewModel.Mode.Inference -> {
                    disableSamplesCollecting()
                    trainButton?.visibility = View.GONE
                }
            }
        }
    }

    override fun setupViews() {
        super.setupViews()

        for (i in 0 until NUM_OF_CLASSES) {
            val viewId = resources.getIdentifier("class_${i}_btn",
                "id", packageName)
            classButtons[i] = findViewById(viewId)

            ClassTrainingInfoManager.add(ClassTrainingInfo((i + 1).toString()))
        }

        trainButton = findViewById(R.id.train_toggle_button)
        trainButton?.setOnClickListener {
            (exampleFragment as TransferLearningCameraFragment).enableTraining(
                TransferLearningModel.LossConsumer { epoch, loss ->

                })
        }

        captureModelButton = findViewById(R.id.capture_mode_button)
        captureModelButton?.setOnCheckedChangeListener { _, b ->
            if (b) {
                viewModel.changeModeTo(TransferLearningViewModel.Mode.Capture)
            }
        }

        inferenceModelButton = findViewById(R.id.inference_mode_button)
        inferenceModelButton?.setOnCheckedChangeListener { _, b ->
            if (b) {
                viewModel.changeModeTo(TransferLearningViewModel.Mode.Inference)
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

    override fun onResultsUpdated(results: Array<TransferLearningModel.Prediction>) {
        val mode = viewModel.mode.value
        if (mode == TransferLearningViewModel.Mode.Inference) {
            for (prediction in results) {
                val classIndex = prediction.className.toInt() - 1

                classButtons[classIndex]?.text = prediction.confidence.toString()
            }
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

    private fun attachClassTrainingInfo(classes: List<ClassTrainingInfo>) {
        for (sampleClass in classes) {
            val classIndex = sampleClass.className.toInt() - 1
            val numOfSamples = sampleClass.numOfSamples
            val lastBitmap = sampleClass.lastSample

            classButtons[classIndex]?.text =
                numOfSamples.toString()
            classButtons[classIndex]?.icon =
                BitmapDrawable(resources, lastBitmap)
        }
    }

    private fun enableSamplesCollecting() {
        for (i in classButtons.indices) {
            classButtons[i]?.setOnClickListener(addSampleListener)
        }
    }

    private fun disableSamplesCollecting() {
        for (i in classButtons.indices) {
            classButtons[i]?.setOnClickListener(null)
        }
    }

    private val addSampleListener = View.OnClickListener { view ->
        val sampleClass = dumpSampleClassFromId(view.id)

        Logger.debug("[NOS] click on sample class: $sampleClass")

        sampleClass?.let {
            (exampleFragment as? TransferLearningCameraFragment)?.addSample(it)

            viewModel.addSample(sampleClass)
        }
    }
}
