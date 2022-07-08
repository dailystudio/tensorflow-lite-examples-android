package com.dailystudio.tflite.example.image.segmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Size
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tensorflow.litex.image.AdvanceInferenceInfo
import org.tensorflow.lite.examples.imagesegmentation.ImageSegmentationModelExecutor
import org.tensorflow.lite.examples.imagesegmentation.SegmentationResult
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.LiteModel
import com.dailystudio.tensorflow.litex.fragment.ImageLiteUseCase
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs

class SegmentationUseCase: ImageLiteUseCase<SegmentationResult, AdvanceInferenceInfo>() {

    companion object {
        const val UC_NAME = "segmentation"

        const val MODEL_IMAGE_SIZE = 257

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val MASK_IMAGE_FILE = "mask.png"
        private const val EXTRACTED_IMAGE_FILE = "extracted.png"
    }

    override fun analyzeFrame(
        inferenceBitmap: Bitmap,
        info: AdvanceInferenceInfo
    ): SegmentationResult? {
        val inferenceResult =
            (defaultModel as? ImageSegmentationModelExecutor)
                ?.execute(inferenceBitmap) ?: return null

        info.preProcessTime = inferenceResult.preProcessTime
        info.flattenTime = inferenceResult.maskFlatteningTime

        val mask = trimBitmap(inferenceResult.bitmapMaskOnly, info.frameSize)
        val extracted = ImageUtils.maskBitmap(
            trimBitmap(inferenceBitmap, info.frameSize), mask)

        val results = SegmentationResult(mask, inferenceResult.itemsFound)
        dumpIntermediateBitmap(mask, MASK_IMAGE_FILE)
        dumpIntermediateBitmap(extracted, EXTRACTED_IMAGE_FILE)

        return results
    }

    override fun createInferenceInfo(): AdvanceInferenceInfo {
        return AdvanceInferenceInfo()
    }

    fun trimBitmap(maskBitmap: Bitmap,
                   frameSize: Size
    ): Bitmap {
        val clipSize = if (frameSize.width > frameSize.height) {
            Size(maskBitmap.width,
                maskBitmap.height * frameSize.height / frameSize.width )
        } else {
            Size(maskBitmap.width * frameSize.width / frameSize.height, maskBitmap.height)
        }

        val xOffset = (maskBitmap.width - clipSize.width) / 2
        val yOffset = (maskBitmap.height - clipSize.height) / 2

        return ImageUtils.createClippedBitmap(maskBitmap,
            xOffset, yOffset, clipSize.width, clipSize.height)
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: AdvanceInferenceInfo
    ): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        if (info.imageRotation % 90 == 0) {
            info.frameSize = Size(frameBitmap.height, frameBitmap.width)
        } else {
            info.frameSize = Size(frameBitmap.width, frameBitmap.height)
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, MODEL_IMAGE_SIZE, MODEL_IMAGE_SIZE,
            info.imageRotation, true, fitIn = true)

        val preScaledBitmap =  ImageUtils.createTransformedBitmap(frameBitmap,
            matrix, paddingColor = Color.BLACK)

        dumpIntermediateBitmap(preScaledBitmap, PRE_SCALED_IMAGE_FILE)

        return preScaledBitmap
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            ImageSegmentationModelExecutor(context, device, numOfThreads, useXNNPack)
        )
    }

}