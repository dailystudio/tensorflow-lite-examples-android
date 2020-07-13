package com.dailystudio.tflite.example.image.segmentation.fragment

import android.graphics.Bitmap
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import org.tensorflow.lite.examples.imagesegmentation.ImageSegmentationModelExecutor
import org.tensorflow.lite.examples.imagesegmentation.ModelExecutionResult

private class ImageSegmentationAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<ImageInferenceInfo, ModelExecutionResult>(rotation, lensFacing) {

    private var segmentationModel: ImageSegmentationModelExecutor? = null

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): ModelExecutionResult? {
        var results: ModelExecutionResult? = null

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
            results = model.execute(inferenceBitmap)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
        }

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
       return ImageInferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, 640, 480, 0, true)

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix)
    }

}

class ImageSegmentationCameraFragment : AbsExampleCameraFragment<ImageInferenceInfo, ModelExecutionResult>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int)
            : AbsImageAnalyzer<ImageInferenceInfo, ModelExecutionResult> {
        return ImageSegmentationAnalyzer(rotation, lensFacing)
    }

}