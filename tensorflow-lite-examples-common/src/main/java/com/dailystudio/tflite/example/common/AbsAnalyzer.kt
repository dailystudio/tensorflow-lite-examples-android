package com.dailystudio.tflite.example.common

import android.content.Context
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.litex.TFLiteModel
import org.tensorflow.lite.support.model.Model.Device

class AvgTime(private val capacity: Int = 10) {

    private val timeValues = Array<Long>(capacity) { 0 }
    private var wIndex = 0

    val value: Long
        get() {
            val len = timeValues.size
            var sum = 0L

            for (i in 0 until len) {
                sum += timeValues[i]
            }
            return sum / len
        }


    fun record(newValue: Long) {
        timeValues[wIndex] = newValue
        wIndex = (wIndex + 1) % capacity
    }

}

abstract class AbsTFLiteModelRunner<Model: TFLiteModel, Input, Info: InferenceInfo, Results>(
    var useAverageTime: Boolean = true,
) {

    protected var model: Model? = null

    private var inferenceAgent: InferenceAgent<Info, Results> =
        InferenceAgent()

    private val avgInferenceTime = AvgTime(20)
    private val avgAnalyzeTime = AvgTime(20)

    init {
        inferenceAgent.deliverInferenceInfo(createInferenceInfo())
    }

    @Synchronized
    open fun onInferenceSettingsChange(changePrefName: String, inferenceSettings: InferenceSettingsPrefs) {
        Logger.debug("[ANALYZER UPDATE]: changed preference: $changePrefName")

        when (changePrefName) {
            InferenceSettingsPrefs.PREF_DEVICE,
            InferenceSettingsPrefs.PREF_NUMBER_OF_THREADS -> {
                invalidateModel()
            }

            InferenceSettingsPrefs.PREF_USER_AVERAGE_TIME -> {
                useAverageTime = inferenceSettings.userAverageTime
            }
        }
    }

    @Synchronized
    open fun run(data: Input, settings: InferenceSettingsPrefs) {
        if (model == null) {
            prepareModel(settings)
        }

        model?.let {
            runInference(it, data)
        }
    }

    open fun destroyModel() {
        model?.close()
    }

    protected open fun prepareModel(settings: InferenceSettingsPrefs) {
        val context = GlobalContextWrapper.context
        context?.let {
            val deviceStr = settings.device

            val device = try {
                Device.valueOf(deviceStr)
            } catch (e: Exception) {
                Logger.warn("cannot parse device from [$deviceStr]: $e")

                Device.CPU
            }

            val threads = settings.numberOfThreads
            Logger.debug("[ANALYZER UPDATE] creating model: device = $device, threads = $threads")

            model = createModel(it, device, threads, settings)
            Logger.debug("[ANALYZER UPDATE] new model created: $model")
        }

        Logger.debug("using model: $model")
    }

    @Synchronized
    protected open fun invalidateModel() {
        destroyModel()

        model = null
        Logger.debug("[ANALYZER UPDATE] model is invalidated")
    }

    protected open fun runInference(model: Model, data: Input) {
        var results: Results? = null
        val info: Info = createInferenceInfo()

        val start = System.currentTimeMillis()
        results = analyze(model, data, info)
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

        Logger.debug("[AVG: ${useAverageTime}] analysis [in ${info.analysisTime} ms (inference: ${info.inferenceTime} ms)]: result = ${results.toString().replace("%", "%%")}")

        inferenceAgent.deliverInferenceInfo(info)

        results?.let {
            inferenceAgent.deliverResults(it)
        }
    }

    protected open fun setResultsUpdateInterval(interval: Long) {
        inferenceAgent.resultsUpdateInterval = interval
    }

    protected abstract fun createModel(context: Context,
                                       device: Device,
                                       numOfThreads: Int,
                                       settings: InferenceSettingsPrefs): Model?

    protected abstract fun analyze(model: Model,
                                   data: Input,
                                   info: Info): Results?

    abstract fun createInferenceInfo(): Info

}