package com.dailystudio.tflite.example.transfer.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.transfer.model.OndeviceLearningModel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition


private class OndeviceLearningAnalyzer(rotation: Int,
                                      lensFacing: Int,
                                      useAverageTime: Boolean,
) : AbsImageAnalyzer<OndeviceLearningModel, ImageInferenceInfo, List<Recognition>>(rotation, lensFacing, useAverageTime) {

    companion object {
        const val TF_MODEL_PATH = ""
    }

    override fun analyzeFrame(
        model: OndeviceLearningModel,
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
    ): OndeviceLearningModel? {
        return OndeviceLearningModel(context,
            TF_MODEL_PATH, device, numOfThreads)
    }

}

class OndeviceLearningCameraFragment : AbsExampleCameraFragment<OndeviceLearningModel, ImageInferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<OndeviceLearningModel, ImageInferenceInfo, List<Recognition>> {
        return OndeviceLearningAnalyzer(rotation, lensFacing, useAverageTime)
    }

}