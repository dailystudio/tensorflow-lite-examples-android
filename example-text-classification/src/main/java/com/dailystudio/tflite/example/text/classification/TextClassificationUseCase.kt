package com.dailystudio.tflite.example.text.classification

import android.content.Context
import com.dailystudio.tensorflow.litex.InferenceInfo
import org.tensorflow.lite.examples.textclassification.TextClassificationClient
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.LiteModel
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs
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