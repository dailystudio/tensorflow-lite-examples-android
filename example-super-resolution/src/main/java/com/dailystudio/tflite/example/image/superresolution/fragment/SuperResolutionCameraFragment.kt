package com.dailystudio.tflite.example.image.superresolution.fragment

import android.os.Bundle
import android.view.View
import androidx.camera.core.ImageProxy
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.image.superresolution.R
import com.dailystudio.tflite.example.image.superresolution.SuperResUseCase
import com.dailystudio.tflite.example.image.superresolution.ui.SelectOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.dailystudio.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class SuperResolutionCameraFragment : LiteCameraUseCaseFragment() {

    private var selectedOverlay: SelectOverlay? = null
    private var oldOverlayWidth: Int = 0
    private var oldImageWidth: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
    }

    private fun setupViews(fragmentView: View) {
        selectedOverlay = fragmentView.findViewById(R.id.selected_overlay)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example_super_resolution
    }

    override fun performUseCasesOnImage(image: ImageProxy) {
        lifecycleScope.launch(Dispatchers.Main) {
            val overlayWidth = selectedOverlay?.width ?: return@launch
            val imageWidth = image.height

            if (oldOverlayWidth == overlayWidth && oldImageWidth == imageWidth) {
                return@launch
            }

            val factor = overlayWidth.toFloat() / imageWidth
            Logger.debug("[SCALE]: image is [${image.width} x ${image.height}]")
            Logger.debug("[SCALE]: overlay is [${selectedOverlay?.width ?: 0} x ${selectedOverlay?.height ?: 0}]")
            Logger.debug("[SCALE]: factor = $factor")

            selectedOverlay?.setScaleFactor(factor)

            oldOverlayWidth = overlayWidth
            oldImageWidth = imageWidth
        }

        super.performUseCasesOnImage(image)
    }

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(SuperResUseCase.UC_NAME)
}