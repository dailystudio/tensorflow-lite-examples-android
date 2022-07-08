package com.dailystudio.tflite.example.image.styletransfer

import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.image.styletransfer.fragment.PickStyleDialogFragment
import com.dailystudio.tflite.example.image.styletransfer.fragment.StyleTransferCameraFragment
import com.dailystudio.tflite.example.image.styletransfer.fragment.StyleTransferSettingsFragment
import com.dailystudio.tflite.example.image.styletransfer.ui.StyledOverlay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.styletransfer.StyleTransferResult
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity
import com.dailystudio.tflite.example.image.styletransfer.model.StyleImageViewModel


class ExampleActivity: LiteUseCaseActivity() {

    companion object {
        const val STYLES_DIRECTORY = "thumbnails"
    }

    private var styledOverlay: StyledOverlay? = null
    private var fabStyles: FloatingActionButton? = null
    private var fabMode: FloatingActionButton? = null
    private var fabCameraSelector: FloatingActionButton? = null

    private var fragmentCamera: StyleTransferCameraFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            loadStyleImages()
        }
    }

    override fun setupViews() {
        super.setupViews()

        styledOverlay = findViewById(R.id.styled_overlay)

        fabStyles = findViewById(R.id.fab_styles)
        fabStyles?.setOnClickListener {
            val dialog = PickStyleDialogFragment()

            dialog.show(supportFragmentManager, "pick-styles")
        }

        fabMode = findViewById(R.id.fab_mode)
        fabMode?.setOnClickListener {
            styledOverlay?.let { overlay ->
                val previewMode = overlay.isInPreviewMode()
                overlay.setPreviewMode(!previewMode)

                fabMode?.setImageResource(if (previewMode) {
                    R.drawable.ic_preview_mode
                } else {
                    R.drawable.ic_fullscreen_mode
                })
            }
        }

        fabCameraSelector = findViewById(R.id.fab_camera_selector)
        fabCameraSelector?.setOnClickListener {
            fragmentCamera?.let {
                val lensFacing = it.getCurrentLensFacing()
                if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    it.changeLensFacing(CameraSelector.LENS_FACING_BACK)
                    fabCameraSelector?.setImageResource(R.drawable.ic_front_camera)
                } else {
                    it.changeLensFacing(CameraSelector.LENS_FACING_FRONT)
                    fabCameraSelector?.setImageResource(R.drawable.ic_back_camera)
                }
            }
        }
    }
    override fun getLayoutResId(): Int {
        return R.layout.activity_example_style_transfer
    }

    override fun createBaseFragment(): Fragment {
        val fragment = StyleTransferCameraFragment()

        fragmentCamera = fragment

        return fragment
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        when(nameOfUseCase) {
            StyleTransferUseCase.UC_NAME -> {
                if (results is StyleTransferResult) {
                    styledOverlay?.setStyledOverlay(results.styledBitmap)
                }
            }
        }
    }

    private fun loadStyleImages() {
        val styles: Array<String> = try {
            assets.list(STYLES_DIRECTORY)
        } catch (e: Exception) {
            Logger.error("failed to list styles in [$STYLES_DIRECTORY]: $e")

            null
        } ?: return

        val viewModel = ViewModelProvider(this)[StyleImageViewModel::class.java]

        for ((i, style) in styles.withIndex()) {
            val styleImage = StyleImage(i,
                style,
                "${STYLES_DIRECTORY}/${style}")

            viewModel.insertStyleImage(styleImage)
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

    override fun getExampleThumbVideoResource(): Int {
        return R.raw.style_transfer_720p
    }

    override fun createSettingsFragment(): AbsSettingsDialogFragment? {
        return StyleTransferSettingsFragment()
    }

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            StyleTransferUseCase.UC_NAME to StyleTransferUseCase()
        )
    }

}