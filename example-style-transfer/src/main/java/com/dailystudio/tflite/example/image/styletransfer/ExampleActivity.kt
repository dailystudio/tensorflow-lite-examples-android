package com.dailystudio.tflite.example.image.styletransfer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.image.AdvanceInferenceInfo
import com.dailystudio.tflite.example.image.styletransfer.fragment.StyleTransferCameraFragment
import com.dailystudio.tflite.example.image.styletransfer.ui.StyledOverlay
import org.tensorflow.lite.examples.styletransfer.StyleTransferResult

class ExampleActivity: AbsExampleActivity<AdvanceInferenceInfo, StyleTransferResult>() {

    private var styledOverlay: StyledOverlay? = null

    override fun setupViews() {
        super.setupViews()

        styledOverlay = findViewById(R.id.styled_overlay)
        styledOverlay?.setOnClickListener { _ ->
            styledOverlay?.let {
                it.setPreviewMode(!it.isInPreviewMode())
            }
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_style_transfer
    }

    override fun createBaseFragment(): Fragment {
        return StyleTransferCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createSettingsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: StyleTransferResult) {
        styledOverlay?.setStyledOverlay(results.styledBitmap)
    }

}