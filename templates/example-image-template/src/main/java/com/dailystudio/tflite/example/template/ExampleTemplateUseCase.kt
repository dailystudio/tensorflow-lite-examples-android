package com.dailystudio.tflite.example.template

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.template.model.ExampleTemplateModel
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.LiteModel
import com.dailystudio.tensorflow.litex.fragment.ImageLiteUseCase
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs

class ExampleTemplateUseCase: ImageLiteUseCase<Void, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "exampletemplate"
        const val TF_MODEL_PATH = ""
    }

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): Void? {
        val start = System.currentTimeMillis()
        val results = (defaultModel as? ExampleTemplateModel)?.analyze(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(ExampleTemplateModel(context,
            TF_MODEL_PATH, device, numOfThreads))
    }

}