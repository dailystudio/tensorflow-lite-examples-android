package com.dailystudio.tflite.example.image.segmentation.fragment

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
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import org.tensorflow.lite.examples.imagesegmentation.ImageSegmentationModelExecutor
import org.tensorflow.lite.examples.imagesegmentation.SegmentationResult

class ImageSegmentationInferenceInfo(var frameSize: Size = Size(0, 0)) : ImageInferenceInfo() {

}

private class ImageSegmentationAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<ImageSegmentationInferenceInfo, SegmentationResult>(rotation, lensFacing) {

    companion object {

        const val MODEL_IMAGE_SIZE = 257

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val MASK_IMAGE_FILE = "mask.png"
        private const val EXTRACTED_IMAGE_FILE = "extracted.png"
    }

    private var segmentationModel: ImageSegmentationModelExecutor? = null

    private var preScaleRevertTransform: Matrix? = null

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageSegmentationInferenceInfo): SegmentationResult? {
        var results: SegmentationResult? = null

        if (segmentationModel == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                segmentationModel =
                    ImageSegmentationModelExecutor(context, false)
            }

            Logger.debug("segmentation model created: $segmentationModel")
        }

        segmentationModel?.let { model ->
            val start = System.currentTimeMillis()
            val mask = trimBitmap(model.fastExecute(inferenceBitmap), info.frameSize)
            val extracted = ImageUtils.maskBitmap(
                trimBitmap(inferenceBitmap, info.frameSize), mask)
            results = SegmentationResult(mask,
                preScaleRevertTransform)
            dumpIntermediateBitmap(mask, MASK_IMAGE_FILE)
            dumpIntermediateBitmap(extracted, EXTRACTED_IMAGE_FILE)

            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
        }

        return results
    }

    override fun createInferenceInfo(): ImageSegmentationInferenceInfo {
       return ImageSegmentationInferenceInfo()
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
                                 info: ImageSegmentationInferenceInfo): Bitmap? {
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

        preScaleRevertTransform = Matrix()
        matrix.invert(preScaleRevertTransform)

        val preScaledBitmap =  ImageUtils.createTransformedBitmap(frameBitmap,
            matrix, paddingColor = Color.BLACK)

        dumpIntermediateBitmap(preScaledBitmap, PRE_SCALED_IMAGE_FILE)

        return preScaledBitmap
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return true
    }

}

class ImageSegmentationCameraFragment : AbsExampleCameraFragment<ImageSegmentationInferenceInfo, SegmentationResult>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int)
            : AbsImageAnalyzer<ImageSegmentationInferenceInfo, SegmentationResult> {
        return ImageSegmentationAnalyzer(rotation, lensFacing)
    }

}