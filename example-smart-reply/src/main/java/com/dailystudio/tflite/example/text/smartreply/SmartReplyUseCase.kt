package com.dailystudio.tflite.example.text.smartreply

import android.content.Context
import org.tensorflow.litex.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.examples.smartreply.SmartReply
import org.tensorflow.lite.examples.smartreply.SmartReplyClient
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.LiteUseCase

class SmartReplyUseCase: LiteUseCase<String, Array<SmartReply>, InferenceInfo>() {

    companion object {
        const val UC_NAME = "smartreply"
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            SmartReplyClient(context)
        )
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

    override fun runInference(input: String, info: InferenceInfo): Array<SmartReply>? {
        return (defaultModel as? SmartReplyClient)?.predict(arrayOf(input))
    }
}