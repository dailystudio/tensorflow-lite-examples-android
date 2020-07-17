package com.dailystudio.tflite.example.image.styletransfer

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.image.AdvanceInferenceInfo
import com.dailystudio.tflite.example.image.styletransfer.fragment.PickStyleDialogFragment
import com.dailystudio.tflite.example.image.styletransfer.fragment.StyleImagesListFragment
import com.dailystudio.tflite.example.image.styletransfer.fragment.StyleTransferCameraFragment
import com.dailystudio.tflite.example.image.styletransfer.model.StyleImageViewModel
import com.dailystudio.tflite.example.image.styletransfer.ui.StyledOverlay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.styletransfer.StyleTransferResult


class ExampleActivity: AbsExampleActivity<AdvanceInferenceInfo, StyleTransferResult>() {

    companion object {
        const val STYLES_DIRECTORY = "thumbnails"
    }

    private var styledOverlay: StyledOverlay? = null
    private var fab: FloatingActionButton? = null

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
//                it.setPreviewMode(!it.isInPreviewMode())
            }
        }

        fab = findViewById(R.id.fab_styles)
        fab?.setOnClickListener {
            val dialog = PickStyleDialogFragment()

            dialog.show(supportFragmentManager, "pick-styles")
        }
    }

    private fun hideFloatingActionButton(fab: FloatingActionButton) {
        val params =
            fab.layoutParams as CoordinatorLayout.LayoutParams
        val behavior =
            params.behavior as FloatingActionButton.Behavior?
        if (behavior != null) {
            behavior.isAutoHideEnabled = false
        }
        fab.hide()
    }

    private fun showFloatingActionButton(fab: FloatingActionButton) {
        fab.show()
        val params =
            fab.layoutParams as CoordinatorLayout.LayoutParams
        val behavior =
            params.behavior as FloatingActionButton.Behavior?
        if (behavior != null) {
            behavior.isAutoHideEnabled = true
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