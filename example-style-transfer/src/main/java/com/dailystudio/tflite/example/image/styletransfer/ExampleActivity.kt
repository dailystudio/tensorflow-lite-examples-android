package com.dailystudio.tflite.example.image.styletransfer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.image.AdvanceInferenceInfo
import com.dailystudio.tflite.example.image.styletransfer.fragment.StyleTransferCameraFragment
import com.dailystudio.tflite.example.image.styletransfer.model.StyleImageViewModel
import com.dailystudio.tflite.example.image.styletransfer.ui.StyledOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.styletransfer.StyleTransferResult
import java.lang.Exception

class ExampleActivity: AbsExampleActivity<AdvanceInferenceInfo, StyleTransferResult>() {

    companion object {
        const val STYLES_DIRECTORY = "thumbnails"
    }

    private var styledOverlay: StyledOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            loadStyleImages()
        }
    }

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

    private fun loadStyleImages() {
        val styles: Array<String> = try {
            assets.list(STYLES_DIRECTORY)
        } catch (e: Exception) {
            Logger.error("failed to list styles in [$STYLES_DIRECTORY]: $e")

            null
        } ?: return

        val viewModel = ViewModelProvider(this).get(StyleImageViewModel::class.java)

        for ((i, style) in styles.withIndex()) {
            val styleImage = StyleImage(i,
                style,
                "${STYLES_DIRECTORY}/${style}")

            viewModel.insertStyleImage(styleImage)
        }
    }

}