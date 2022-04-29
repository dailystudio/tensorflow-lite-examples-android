package com.dailystudio.tflite.example.image.ocr

import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.image.ocr.fragment.OpticalCharacterRecognitionCameraFragment
import org.tensorflow.lite.examples.ocr.ModelExecutionResult

class ExampleActivity : AbsExampleActivity<OCRInferenceInfo, ModelExecutionResult>() {

    private var resultOverlay: ImageView? = null

    override fun setupViews() {
        super.setupViews()
        resultOverlay = findViewById(R.id.result_overlay)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_optical_character_recognition
    }

    override fun createBaseFragment(): Fragment {
        return OpticalCharacterRecognitionCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: ModelExecutionResult) {
        Logger.debug("items: ${results.itemsFound}")

        resultOverlay?.setImageBitmap(results.bitmapResult)
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
