package com.dailystudio.tflite.example.image.segmentation

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.image.segmentation.fragment.ImageSegmentationCameraFragment
import com.dailystudio.tflite.example.image.segmentation.fragment.ImageSegmentationInferenceInfo
import kotlinx.android.synthetic.main.activity_example_image_segmentation.*
import org.tensorflow.lite.examples.imagesegmentation.ImageUtils
import org.tensorflow.lite.examples.imagesegmentation.ModelExecutionResult
import org.tensorflow.lite.examples.imagesegmentation.SegmentationResult

class ExampleActivity : AbsExampleActivity<ImageSegmentationInferenceInfo, SegmentationResult>() {

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_image_segmentation
    }

    override fun createBaseFragment(): Fragment {
        return ImageSegmentationCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createSettingsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: SegmentationResult) {
        mask_overlay?.setMask(results.maskBitmap)
    }

}