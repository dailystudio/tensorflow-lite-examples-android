package org.tensorflow.litex

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.preference.PrefsChange
import com.dailystudio.tflite.example.common.AvgTime
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.model.Model


abstract class MLUseCase<LiteModel: TFLiteModel, Input, Result, Info: InferenceInfo>(
    lifecycleOwner: LifecycleOwner,
    val useAverageTime: Boolean = true,
): DefaultLifecycleObserver {

    private val settingsObserver = Observer<PrefsChange> {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            onInferenceSettingsChange(it.prefKey, getSettingsPreference())
        }
    }

    private var inferenceAgent: InferenceAgent<InferenceInfo, Result> =
        InferenceAgent()

    private val avgInferenceTime = AvgTime(20)
    private val avgAnalyzeTime = AvgTime(20)
    private var model: LiteModel? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)

        inferenceAgent.deliverInferenceInfo(createInferenceInfo())
    }

    @Synchronized
    open fun run(data: Input): Result? {
        if (model == null) {
            prepareModel(getSettingsPreference())
        }

        return model?.let {
            runInference(it, data)
        }
    }

    protected open fun runInference(model: LiteModel, data: Input): Result? {
        var results: Result? = null
        val info = createInferenceInfo()

        val start = System.currentTimeMillis()
        results = runInference(model, data, info)
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

        return results?.also {
            inferenceAgent.deliverResults(it)
        }
    }

    protected abstract fun runInference(model: LiteModel,
                                        data: Input,
                                        info: Info
    ): Result?

    @Synchronized
    protected open fun invalidateModel() {
        Logger.debug("[MODEL_CREATION]: model is invalidated")
        destroyModel()

        model = null
    }

    protected open fun prepareModel(settings: InferenceSettingsPrefs) {
        val context = GlobalContextWrapper.context
        context?.let {
            val deviceStr = settings.device

            val device = try {
                Model.Device.valueOf(deviceStr)
            } catch (e: Exception) {
                Logger.warn("cannot parse device from [$deviceStr]: $e")

                Model.Device.CPU
            }

            val threads = settings.numberOfThreads

            val useXNNPack = settings.useXNNPack
            Logger.debug("[MODEL_CREATION] creating model: device = $device, threads = $threads, useXNNPack = $useXNNPack")

            model = createModel(it, device, threads, useXNNPack, settings)
            Logger.debug("[MODEL_CREATION] new model created: $model")
        }

        Logger.debug("using model: $model")
    }

    abstract fun getSettingsPreference(): InferenceSettingsPrefs
    abstract fun createModel(context: Context,
                             device: Model.Device,
                             threads: Int,
                             useXNNPack: Boolean,
                             settings: InferenceSettingsPrefs
    ): LiteModel?

    abstract fun createInferenceInfo(): Info

    open fun destroyModel() {
        model?.close()
    }

    @Synchronized
    open fun onInferenceSettingsChange(changePrefName: String, inferenceSettings: InferenceSettingsPrefs) {
        Logger.debug("[WATCH CHANGE]: changed preference: $changePrefName")

        when (changePrefName) {
            InferenceSettingsPrefs.PREF_DEVICE,
            InferenceSettingsPrefs.PREF_NUMBER_OF_THREADS,
            InferenceSettingsPrefs.PREF_USE_X_N_N_PACK -> {
                invalidateModel()
            }

        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        watchOnSettingsChange(owner)
        Logger.debug("[WATCH ON]: $owner")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        watchOffSettingsChange(owner)
        Logger.debug("[WATCH OFF]: $owner")
    }

    private fun watchOnSettingsChange(owner: LifecycleOwner) {
        val settingsPrefs = getSettingsPreference()
        settingsPrefs.prefsChange.observe(owner,
            settingsObserver)
    }

    private fun watchOffSettingsChange(owner: LifecycleOwner) {
        val settingsPrefs = getSettingsPreference()
        settingsPrefs.prefsChange.observe(owner,
            settingsObserver)
    }
}
