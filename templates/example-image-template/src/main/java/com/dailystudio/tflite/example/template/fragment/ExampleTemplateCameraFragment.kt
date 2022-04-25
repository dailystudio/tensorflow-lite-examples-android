package com.dailystudio.tflite.example.template.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.template.model.ExampleTemplateModel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition


private class ExampleTemplateAnalyzer(rotation: Int,
                                      lensFacing: Int,
                                      useAverageTime: Boolean,
) : AbsImageAnalyzer<ExampleTemplateModel, ImageInferenceInfo, List<Recognition>>(rotation, lensFacing, useAverageTime) {

    companion object {
        const val TF_MODEL_PATH = ""
    }

    override fun analyzeFrame(
        model: ExampleTemplateModel,
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
    ): ExampleTemplateModel? {
        return ExampleTemplateModel(context,
            TF_MODEL_PATH, device, numOfThreads)
    }

}

class ExampleTemplateCameraFragment : AbsExampleCameraFragment<ExampleTemplateModel, ImageInferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<ExampleTemplateModel, ImageInferenceInfo, List<Recognition>> {
        return ExampleTemplateAnalyzer(rotation, lensFacing, useAverageTime)
    }

}