package org.tensorflow.lite.examples.bertqa.ml

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.MLUseCase

class QaUseCase(
    lifecycleOwner: LifecycleOwner
): MLUseCase<QaClient, Pair<String, String>, List<QaAnswer>, InferenceInfo>(lifecycleOwner) {
    override fun runInference(
        model: QaClient,
        data: Pair<String, String>,
        info: InferenceInfo
    ): List<QaAnswer>? {
        val start = System.currentTimeMillis()
        val result = model.predict(data.first, data.second)
        val end = System.currentTimeMillis()

        info.inferenceTime = end - start

        return result
    }

    override fun getSettingsPreference(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        threads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): QaClient? {
        return QaClient(context, device, threads, useXNNPack)
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }
}