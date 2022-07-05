package org.tensorflow.lite.examples.bertqa.ml

import android.content.Context
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.LiteUseCase

class QaUseCase: LiteUseCase<Pair<String, String>, List<QaAnswer>, InferenceInfo>() {

    companion object {
        const val UC_NAME = "bertqa"
    }


    override fun runInference(input: Pair<String, String>, info: InferenceInfo): List<QaAnswer>? {
        val start = System.currentTimeMillis()
        val result = (defaultModel as? QaClient)?.predict(input.first, input.second)
        val end = System.currentTimeMillis()

        info.inferenceTime = end - start

        return result
    }

    override fun getInferenceSettings(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            QaClient(context, device, numOfThreads, useXNNPack)
        )
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

}