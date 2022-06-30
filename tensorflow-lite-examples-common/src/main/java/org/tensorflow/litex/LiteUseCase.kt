package org.tensorflow.litex

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AvgTime
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.support.model.Model

abstract class LiteUseCase<Input, Output, Info: InferenceInfo> {

    companion object {
        private val useCases: MutableMap<String, LiteUseCase<*, *, *>> =
            mutableMapOf()

        fun registerUseCase(name: String, useCase: LiteUseCase<*, *, *>) {
            useCases[name] = useCase
        }

        fun getLiteUseCase(name: String): LiteUseCase<*, *, *>? = useCases[name]
    }

    protected var liteModels: Array<LiteModel>?  = null
    protected val defaultModel: LiteModel?
        get() {
            return liteModels?.getOrNull(0)
        }

    var useAverageTime: Boolean = true
    private val avgInferenceTime = AvgTime(20)
    private val avgAnalyzeTime = AvgTime(20)

    @WorkerThread
    @Synchronized
    protected open fun checkAndPrepareModels(context: Context): Boolean {
        if (liteModels == null) {
            val settings = getInferenceSettings()

            liteModels = createModels(
                context,
                settings.getDevice(),
                settings.numberOfThreads,
                settings.useXNNPack,
                settings
            )

            Logger.debug("[MODEL CREATE]: new created models = $liteModels")

            liteModels?.forEach { model ->
                model.open()
            }
        }

        return !liteModels.isNullOrEmpty()
    }

    @Synchronized
    @WorkerThread
    open fun destroyModels() {
        Logger.debug("destroy models: $liteModels")
        liteModels?.forEach {
            Logger.debug("models [$it] is closing")
            it.close()
        }

        liteModels = null
    }

    open fun invalidateModels() {
        destroyModels()
    }

    open fun runModels(context: Context, input: Input): Pair<Output?, Info> {
        val info = createInferenceInfo()

        if (!checkAndPrepareModels(context)) {
            Logger.warn("models fro use-case are NOT ready yet. skip inference")
            return Pair(null, info)
        }

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

        return Pair(output, info)
    }

    open fun applySettingsChange(changePrefName: String,
                                 inferenceSettings: InferenceSettingsPrefs) {
        Logger.debug("applying changed preference: $changePrefName")

        when (changePrefName) {
            InferenceSettingsPrefs.PREF_DEVICE,
            InferenceSettingsPrefs.PREF_NUMBER_OF_THREADS,
            InferenceSettingsPrefs.PREF_USE_X_N_N_PACK -> {
                invalidateModels()
            }

            InferenceSettingsPrefs.PREF_USE_AVERAGE_TIME -> {
                useAverageTime = inferenceSettings.useAverageTime
            }
        }
    }

    @WorkerThread
    protected abstract fun createModels(
        context: Context,
        device: Model.Device = Model.Device.CPU,
        numOfThreads: Int = 1,
        useXNNPack: Boolean = true,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel>

    protected abstract fun createInferenceInfo(): Info
    abstract fun getInferenceSettings(): InferenceSettingsPrefs

    @WorkerThread
    protected abstract fun runInference(input: Input, info: Info): Output?
}

fun Fragment.getLiteUseCaseViewModel(name: String): LiteUseCaseViewModel? {
    val activity = requireActivity()
    val application = activity.application
    val useCase = LiteUseCase.getLiteUseCase(name) ?: return null

    return ViewModelProvider(
        activity,
        LiteUseCaseViewModelFactory(application, useCase)
    )[LiteUseCaseViewModel::class.java]
}

fun Fragment.observeUseCaseOutput(name: String, observer: Observer<in Any?>) {
    val viewModel = getLiteUseCaseViewModel(name) ?: return
    viewModel.output.observe(this, observer)
}

fun Fragment.observeUseCaseInfo(name: String, observer: Observer<in InferenceInfo>) {
    val viewModel = getLiteUseCaseViewModel(name) ?: return
    viewModel.inferenceInfo.observe(this, observer)
}

fun AppCompatActivity.getLiteUseCaseViewModel(name: String): LiteUseCaseViewModel? {
    val application = this.application
    val useCase = LiteUseCase.getLiteUseCase(name) ?: return null

    return ViewModelProvider(
        this,
        LiteUseCaseViewModelFactory(application, useCase)
    )[LiteUseCaseViewModel::class.java]
}

fun AppCompatActivity.observeUseCaseOutput(name: String, observer: Observer<in Any?>) {
    val viewModel = getLiteUseCaseViewModel(name) ?: return
    viewModel.output.observe(this, observer)
}

fun AppCompatActivity.observeUseCaseInfo(name: String, observer: Observer<in InferenceInfo>) {
    val viewModel = getLiteUseCaseViewModel(name) ?: return
    viewModel.inferenceInfo.observe(this, observer)
}
