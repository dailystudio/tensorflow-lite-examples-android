package com.dailystudio.tflite.example.text.classification

import android.content.Context
import org.tensorflow.litex.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.examples.textclassification.TextClassificationClient
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.LiteUseCase
import java.util.*

class TextClassificationUseCase: LiteUseCase<String, Map<String, TextClassificationClient.Result>, InferenceInfo>() {

    companion object {
        const val UC_NAME = "textclassifier"
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            TextClassificationClient(context, device, numOfThreads, useXNNPack)
        )
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

    override fun runInference(
        input: String,
        info: InferenceInfo
    ): Map<String, TextClassificationClient.Result>? {
        val results = (defaultModel as? TextClassificationClient)?.classify(input)

        val map = mutableMapOf<String, TextClassificationClient.Result>()
        results?.let {
            for (r in it) {
                map[r.title.lowercase(Locale.getDefault())] = r
            }
        }

        return map
    }

}