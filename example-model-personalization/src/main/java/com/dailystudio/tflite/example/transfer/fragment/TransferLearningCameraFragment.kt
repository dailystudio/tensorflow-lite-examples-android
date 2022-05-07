package com.dailystudio.tflite.example.transfer.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.transfer.model.TransferLearningModel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition


private class TransferLearningAnalyzer(rotation: Int,
                                       lensFacing: Int,
                                       useAverageTime: Boolean,
) : AbsImageAnalyzer<TransferLearningModel, ImageInferenceInfo, List<Recognition>>(rotation, lensFacing, useAverageTime) {

    companion object {
    }

    override fun analyzeFrame(
        model: TransferLearningModel,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Recognition>? {
        var results: List<Recognition>? = null

        val start = System.currentTimeMillis()
        results = model.analyze(inferenceBitmap)
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
    ): TransferLearningModel? {
        return TransferLearningModel(context, device, numOfThreads)
    }

}

class TransferLearningCameraFragment : AbsExampleCameraFragment<TransferLearningModel, ImageInferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<TransferLearningModel, ImageInferenceInfo, List<Recognition>> {
        return TransferLearningAnalyzer(rotation, lensFacing, useAverageTime)
    }

}