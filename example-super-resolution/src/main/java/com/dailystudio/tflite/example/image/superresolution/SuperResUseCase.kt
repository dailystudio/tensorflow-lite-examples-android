package com.dailystudio.tflite.example.image.superresolution

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import org.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.superresolution.model.SuperRes
import com.dailystudio.tflite.example.image.superresolution.model.SuperResolutionModel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.fragment.ImageLiteUseCase
import kotlin.math.roundToInt

class SuperResUseCase: ImageLiteUseCase<SuperRes, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "superes"

        const val TF_MODEL_PATH = "ESRGAN.tflite"
    }

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): SuperRes? {
        val start = System.currentTimeMillis()
        val results = (defaultModel as? SuperResolutionModel)?.superResolution(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
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

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            SuperResolutionModel(context, TF_MODEL_PATH, device, numOfThreads, useXNNPack)
        )
    }

}