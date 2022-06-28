package org.tensorflow.litex

import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AvgTime
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.support.model.Model

abstract class LiteUseCase<Input, Output, Info: InferenceInfo>(
    settings: InferenceSettingsPrefs
) {

    private val liteModels: Array<LiteModel> by lazy {
        createModels(
            settings.getDevice(),
            settings.numberOfThreads,
            settings.useXNNPack,
            settings
        )
    }

    protected val defaultModel: LiteModel? = liteModels.getOrNull(0)

    protected open val useAverageTime: Boolean = true
    private val avgInferenceTime = AvgTime(20)
    private val avgAnalyzeTime = AvgTime(20)

    open fun prepare() {
        liteModels.forEach {
            it.open()
        }
    }

    open fun destroy() {
        liteModels.forEach {
            it.close()
        }
    }

    open fun perform(input: Input, info: Info): Output? {
        val start = System.currentTimeMillis()
        val output = runInference(input, info)
        val end = System.currentTimeMillis()

        if (info.analysisTime == 0L) {
            info.analysisTime = (end - start)
        }

        if (info.inferenceTime == 0L) {
            info.inferenceTime = info.analysisTime
        }

        if (useAverageTime) {
            avgInferenceTime.record(info.inferenceTime)
            avgAnalyzeTime.record(info.analysisTime)

            info.inferenceTime = avgInferenceTime.value
            info.analysisTime = avgAnalyzeTime.value
        }

        Logger.debug("[AVG: ${useAverageTime}] analysis [in ${info.analysisTime} ms (inference: ${info.inferenceTime} ms)]: result = ${output.toString().replace("%", "%%")}")

        return output
    }

    protected abstract fun createModels(
        device: Model.Device = Model.Device.CPU,
        numOfThreads: Int = 1,
        useXNNPack: Boolean = true,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel>

    protected abstract fun runInference(data: Input, info: InferenceInfo): Output?
}

fun InferenceSettingsPrefs.getDevice(): Model.Device {
    val deviceStr = device

    return try {
        Model.Device.valueOf(deviceStr)
    } catch (e: Exception) {
        Logger.warn("cannot parse device from [$deviceStr]: $e")

        Model.Device.CPU
    }
}
