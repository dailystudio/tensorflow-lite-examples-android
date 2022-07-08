package com.dailystudio.tensorflow.litex

import android.app.Application
import androidx.lifecycle.*
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.preference.PrefsChange
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs

private data class ManagedUseCase(
    val useCase: LiteUseCase<*, *, *>,
    val output: MutableLiveData<*>,
    val info: MutableLiveData<InferenceInfo>
)

open class LiteUseCaseViewModel(
    application: Application,
): AndroidViewModel(application) {

    private val managedUseCases: MutableMap<String, ManagedUseCase> =
        mutableMapOf()

    fun manageUseCase(nameOfUseCase: String, useCase: LiteUseCase<*, *, *>) {
        viewModelScope.launch {
            val settings = useCase.getInferenceSettings()
            settings.prefsChanges.collect {
                val changePrefName = it.prefKey

                useCase.applySettingsChange(changePrefName, settings)
            }
        }

        val infoLiveData = MutableLiveData(useCase.createInferenceInfo())
        val outputLiveData = MutableLiveData(null)

        managedUseCases[nameOfUseCase] =
            ManagedUseCase(useCase, outputLiveData, infoLiveData)
    }

    fun buildUseCase (
        nameOfUseCase: String,
        useCaseKlass: Class<out LiteUseCase<*, *, *>>
    ): LiteUseCase<*, *, *>? {
        val useCase: LiteUseCase<*, *, *> = try {
            useCaseKlass.newInstance()
        } catch (e: IllegalAccessException) {
            Logger.error("failed to create use-case [$useCaseKlass]: $e")
            null
        } catch (e: InstantiationException) {
            Logger.error("failed to create use-case [$useCaseKlass]: $e")
            null
        } ?: return null

        manageUseCase(nameOfUseCase, useCase)

        return useCase
    }

    fun getUseCase(nameOfUseCase: String): LiteUseCase<*, *, *>? {
        return managedUseCases[nameOfUseCase]?.useCase
    }

    fun performUseCase(nameOfUseCase: String, input: Any): Any? {
        val managedUseCase =
            managedUseCases[nameOfUseCase] ?: return null

        val useCase = managedUseCase.useCase as LiteUseCase<Any, Any, *>
        val liveDataOfOutput = managedUseCase.output as MutableLiveData<Any>
        val liveDataOfInfo = managedUseCase.info

        val results = useCase.runModels(getApplication(), input)

        liveDataOfInfo.postValue(results.second)
        liveDataOfOutput.postValue(results.first)

        return results.first
    }

    fun observeUseCaseOutput(lifecycleOwner: LifecycleOwner,
                             nameOfUseCase: String,
                             observer: Observer<in Any?>) {
        val managedUseCase =
            managedUseCases[nameOfUseCase] ?: return

        managedUseCase.output.observe(lifecycleOwner, observer)
    }

    fun observeUseCaseInfo(lifecycleOwner: LifecycleOwner,
                           nameOfUseCase: String,
                           observer: Observer<in InferenceInfo>) {
        val managedUseCase =
            managedUseCases[nameOfUseCase] ?: return

        (managedUseCase.info).observe(lifecycleOwner, observer)
    }

    fun observeUseCaseSettings(lifecycleOwner: LifecycleOwner,
                               nameOfUseCase: String,
                               observer: Observer<in PrefsChange>) {
        val managedUseCase =
            managedUseCases[nameOfUseCase] ?: return

        managedUseCase.useCase.getInferenceSettings()
            .prefsChange.observe(lifecycleOwner, observer)
    }

    override fun onCleared() {
        managedUseCases.forEach {
            it.value.useCase.destroyModels()
        }
    }

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