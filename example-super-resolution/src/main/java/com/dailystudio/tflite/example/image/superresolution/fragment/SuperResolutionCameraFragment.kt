package com.dailystudio.tflite.example.image.superresolution.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.view.View
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
import com.dailystudio.tflite.example.image.superresolution.ui.ClickOverlay
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition
import kotlin.math.roundToInt

private class SuperResolutionAnalyzer(rotation: Int,
                                      lensFacing: Int,
                                      useAverageTime: Boolean,
) : AbsImageAnalyzer<SuperResolutionModel, ImageInferenceInfo, SuperRes>(rotation, lensFacing, useAverageTime) {

    companion object {
        const val TF_MODEL_PATH = "ESRGAN.tflite"
    }

    private var visibleArea: RectF = RectF()
    private var clipArea: RectF = RectF()

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
        settings: InferenceSettingsPrefs
    ): SuperResolutionModel? {
        return SuperResolutionModel(context,
            TF_MODEL_PATH, device, numOfThreads)
    }

    fun setClipInfo(visible: RectF, clip: RectF) {
        visibleArea = visible
        clipArea = clip
    }

    override fun preProcessImage(frameBitmap: Bitmap?, info: ImageInferenceInfo): Bitmap? {
        val bitmap = frameBitmap ?: return frameBitmap
        Logger.debug("[CLIP]: original bitmap is [${bitmap.width} x ${bitmap.height}]")
        Logger.debug("[CLIP]: visible = [${visibleArea}], clip = [${clipArea}]")

        if (clipArea.isEmpty) {
            return frameBitmap
        }

        val rotatedBitmap = ImageUtils.rotateBitmap(bitmap, info.imageRotation)

        val matrix = MatrixUtils.getTransformationMatrix(
            rotatedBitmap.width, rotatedBitmap.height,
            visibleArea.width().roundToInt(),
            visibleArea.height().roundToInt(),
            0,
            maintainAspectRatio = true,
            fitIn = false
        )

        val invertMatrix = Matrix()
        matrix.invert(invertMatrix)

        val mappedArea = RectF(clipArea)
        invertMatrix.mapRect(mappedArea)
        Logger.debug("[CLIP]: mapped clip area: [${mappedArea}]")
        Logger.debug("[CLIP]: mapped clip w: [${mappedArea.width()}]")
        Logger.debug("[CLIP]: mapped clip h: [${mappedArea.height()}]")

        val scale = 50f / mappedArea.width()
        val cx = mappedArea.centerX()
        val cy = mappedArea.centerY()
        mappedArea.set(
            cx - 25f, cy - 25f,
            cx + 25f, cy + 25f,
        )

        return ImageUtils.createClippedBitmap(rotatedBitmap,
            mappedArea.left.roundToInt(), mappedArea.top.roundToInt(),
            mappedArea.width().roundToInt(), mappedArea.height().roundToInt()
        )
    }

}

class SuperResolutionCameraFragment : AbsExampleCameraFragment<SuperResolutionModel, ImageInferenceInfo, SuperRes>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
    }

    private fun setupViews(fragmentView: View) {
        val clickOverlay: ClickOverlay? = fragmentView.findViewById(R.id.click_overlay)
        clickOverlay?.selectedAreaLiveData?.observe(viewLifecycleOwner) {
            Logger.debug("analyze area: $it")

            (analyzer as? SuperResolutionAnalyzer)?.setClipInfo(
                it.visibleArea, it.clickedArea)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example_super_resolution
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