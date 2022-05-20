package com.dailystudio.tflite.example.image.superresolution.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import androidx.camera.core.ImageProxy
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.superresolution.R
import com.dailystudio.tflite.example.image.superresolution.model.SuperRes
import com.dailystudio.tflite.example.image.superresolution.model.SuperResolutionModel
import com.dailystudio.tflite.example.image.superresolution.ui.SelectOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.model.Model
import kotlin.math.roundToInt

private class SuperResolutionAnalyzer(rotation: Int,
                                      lensFacing: Int,
                                      useAverageTime: Boolean,
) : AbsImageAnalyzer<SuperResolutionModel, ImageInferenceInfo, SuperRes>(rotation, lensFacing, useAverageTime) {

    companion object {
        const val TF_MODEL_PATH = "ESRGAN.tflite"
    }

    override fun analyzeFrame(
        model: SuperResolutionModel,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): SuperRes? {
        var results: SuperRes? = null

        val start = System.currentTimeMillis()
        results = model.superResolution(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): SuperResolutionModel? {
        return SuperResolutionModel(context,
            TF_MODEL_PATH, device, numOfThreads, useXNNPack)
    }

    override fun preProcessImage(frameBitmap: Bitmap?, info: ImageInferenceInfo): Bitmap? {
        val bitmap = frameBitmap ?: return frameBitmap
        Logger.debug("[CLIP]: original bitmap is [${bitmap.width} x ${bitmap.height}]")

        val rotatedBitmap = ImageUtils.rotateBitmap(bitmap, info.imageRotation)

        val size = SuperResolutionModel.INPUT_IMAGE_SIZE
        val x = rotatedBitmap.width / 2f - size / 2f
        val y = rotatedBitmap.height / 2f - size / 2f

        val clipArea = RectF(
            x, y,
            x + size, y + size
        )

        return ImageUtils.createClippedBitmap(rotatedBitmap,
            clipArea.left.roundToInt(), clipArea.top.roundToInt(),
            clipArea.width().roundToInt(), clipArea.height().roundToInt()
        )
    }

}

class SuperResolutionCameraFragment : AbsExampleCameraFragment<SuperResolutionModel, ImageInferenceInfo, SuperRes>() {

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

    override fun runAnalyzer(image: ImageProxy) {
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

        super.runAnalyzer(image)
    }

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<SuperResolutionModel, ImageInferenceInfo, SuperRes> {
        return SuperResolutionAnalyzer(rotation, lensFacing, useAverageTime)
    }

}