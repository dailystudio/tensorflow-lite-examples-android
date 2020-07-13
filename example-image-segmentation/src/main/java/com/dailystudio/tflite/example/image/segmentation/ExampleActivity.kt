package com.dailystudio.tflite.example.image.segmentation

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.image.segmentation.fragment.ImageSegmentationCameraFragment
import org.tensorflow.lite.examples.imagesegmentation.ModelExecutionResult

class ExampleActivity : AbsExampleActivity<ImageInferenceInfo, ModelExecutionResult>() {

    override fun createBaseFragment(): Fragment {
        return ImageSegmentationCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createSettingsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: ModelExecutionResult) {
    }

}