package com.dailystudio.tensorflow.litex

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.development.Logger
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs
import kotlin.math.min

class AvgTime(private val capacity: Int = 10) {

    private val timeValues = Array<Long>(capacity) { 0 }
    private var wIndex = 0
    private var count = 0

    val value: Long
        get() {
            val len = min(timeValues.size, count)
            var sum = 0L

            for (i in 0 until len) {
                sum += timeValues[i]
            }

            return sum / len
        }


    fun record(newValue: Long) {
        timeValues[wIndex] = newValue
        wIndex = (wIndex + 1) % capacity
        count++
    }

}

abstract class LiteUseCase<Input, Output, Info: InferenceInfo> {

    protected var liteModels: Array<LiteModel>?  = null
    protected val defaultModel: LiteModel?
        get() {
            return liteModels?.getOrNull(0)
        }
    protected val lockOfModels = Object()

    var useAverageTime: Boolean = true
    private val avgInferenceTime = AvgTime(20)
    private val avgAnalyzeTime = AvgTime(20)

    @WorkerThread
    protected open fun checkAndPrepareModels(context: Context): Boolean {
        return synchronized(lockOfModels) {
            if (liteModels == null) {
                val settings = getInferenceSettings()

                liteModels = createModels(
                    context,
                    settings.getDevice(),
                    settings.numberOfThreads,
                    settings.useXNNPack,
                    settings
                )

                Logger.debug("[MODELS INSTANCE]: new created models = $liteModels")

                liteModels?.forEach { model ->
                    model.open()
                }
            }

            !liteModels.isNullOrEmpty()
        }

    }

    @WorkerThread
    open fun destroyModels() {
        synchronized(lockOfModels) {
            Logger.debug("[MODELS INSTANCE]: models to destroy = $liteModels")
            liteModels?.forEach {
                Logger.debug("models [$it] is closing")
                it.close()
            }

            liteModels = null
        }
    }

    open fun invalidateModels() {
        Logger.debug("[MODELS INSTANCE]: models to invalidate = $liteModels")
        destroyModels()
    }

    open fun runModels(context: Context, input: Input): Pair<Output?, Info> {
        val info = createInferenceInfo()

        if (!checkAndPrepareModels(context)) {
            Logger.warn("models fro use-case are NOT ready yet. skip inference")
            return Pair(null, info)
        }

        val start = System.currentTimeMillis()
        val output = synchronized(lockOfModels) {
            runInference(input, info)
        }
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

    open fun getInferenceSettings(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

    @WorkerThread
    protected abstract fun createModels(
        context: Context,
        device: Model.Device = Model.Device.CPU,
        numOfThreads: Int = 1,
        useXNNPack: Boolean = true,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel>

    abstract fun createInferenceInfo(): Info

    @WorkerThread
    protected abstract fun runInference(input: Input, info: Info): Output?
}

fun Fragment.getLiteUseCaseViewModel(): LiteUseCaseViewModel {
    val activity = requireActivity()

    return ViewModelProvider(
        activity)[LiteUseCaseViewModel::class.java]
}

fun Fragment.observeUseCaseOutput(name: String, observer: Observer<in Any?>) {
    val viewModel = getLiteUseCaseViewModel()
    viewModel.observeUseCaseOutput(this, name, observer)
}

fun Fragment.observeUseCaseInfo(name: String, observer: Observer<in InferenceInfo>) {
    val viewModel = getLiteUseCaseViewModel() ?: return
    viewModel.observeUseCaseInfo(this, name, observer)
}

fun AppCompatActivity.getLiteUseCaseViewModel(): LiteUseCaseViewModel {
    return ViewModelProvider(this)[LiteUseCaseViewModel::class.java]
}

fun AppCompatActivity.observeUseCaseOutput(name: String, observer: Observer<in Any?>) {
    val viewModel = getLiteUseCaseViewModel()
    viewModel.observeUseCaseOutput(this, name, observer)
}

fun AppCompatActivity.observeUseCaseInfo(name: String, observer: Observer<in InferenceInfo>) {
    val viewModel = getLiteUseCaseViewModel()
    viewModel.observeUseCaseInfo(this, name, observer)
}
