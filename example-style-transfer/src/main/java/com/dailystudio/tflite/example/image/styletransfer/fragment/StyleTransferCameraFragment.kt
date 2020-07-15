package com.dailystudio.tflite.example.image.styletransfer.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Size
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AdvanceInferenceInfo
import org.tensorflow.lite.examples.styletransfer.StyleTransferModelExecutor
import org.tensorflow.lite.examples.styletransfer.StyleTransferResult

private class StyleTransferAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<AdvanceInferenceInfo, StyleTransferResult>(rotation, lensFacing) {

    companion object {

        const val STYLE_IMAGE_SIZE = 384

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val MASK_IMAGE_FILE = "mask.png"
        private const val EXTRACTED_IMAGE_FILE = "extracted.png"
    }

    private var styleTransferModelExecutor: StyleTransferModelExecutor? = null

    private var preScaleRevertTransform: Matrix? = null

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: AdvanceInferenceInfo): StyleTransferResult? {
        var results: StyleTransferResult? = null

        if (styleTransferModelExecutor == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                styleTransferModelExecutor =
                    StyleTransferModelExecutor(context, false)
            }

            Logger.debug("segmentation model created: $styleTransferModelExecutor")
        }

        styleTransferModelExecutor?.let { model ->
//            val inferenceResult = model.execute(inferenceBitmap, info)
            val inferenceResult = Pair<Bitmap, Set<String>>(inferenceBitmap, setOf())
            val mask = trimBitmap(inferenceResult.first, info.frameSize)
            val extracted = ImageUtils.maskBitmap(
                trimBitmap(inferenceBitmap, info.frameSize), mask)
            results = StyleTransferResult(mask, inferenceResult.second)
            dumpIntermediateBitmap(mask, MASK_IMAGE_FILE)
            dumpIntermediateBitmap(extracted, EXTRACTED_IMAGE_FILE)
        }

        return results
    }

    override fun createInferenceInfo(): AdvanceInferenceInfo {
       return AdvanceInferenceInfo()
    }

    fun trimBitmap(maskBitmap: Bitmap,
                   frameSize: Size): Bitmap {
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
                                 info: AdvanceInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        if (info.imageRotation % 90 == 0) {
            info.frameSize = Size(frameBitmap.height, frameBitmap.width)
        } else {
            info.frameSize = Size(frameBitmap.width, frameBitmap.height)
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, STYLE_IMAGE_SIZE, STYLE_IMAGE_SIZE,
            info.imageRotation, true, fitIn = true)

        preScaleRevertTransform = Matrix()
        matrix.invert(preScaleRevertTransform)

        val preScaledBitmap =  ImageUtils.createTransformedBitmap(frameBitmap,
            matrix, paddingColor = Color.BLACK)

        dumpIntermediateBitmap(preScaledBitmap, PRE_SCALED_IMAGE_FILE)

        return preScaledBitmap
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

}

class StyleTransferCameraFragment : AbsExampleCameraFragment<AdvanceInferenceInfo, StyleTransferResult>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int)
            : AbsImageAnalyzer<AdvanceInferenceInfo, StyleTransferResult> {
        return StyleTransferAnalyzer(rotation, lensFacing)
    }

}