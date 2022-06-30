package org.tensorflow.litex

import android.app.Application
import androidx.lifecycle.*
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.model.Model
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException

class LiteUseCaseViewModelFactory(private val application: Application,
                                  private val useCase: LiteUseCase<*, *, *>)
    : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (LiteUseCaseViewModel::class.java.isAssignableFrom(modelClass)) {
            try {
                modelClass.getConstructor(Application::class.java,
                    LiteUseCase::class.java).newInstance(application, useCase)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InstantiationException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }
        } else super.create(modelClass)
    }
}

open class LiteUseCaseViewModel(
    application: Application,
    private val useCase: LiteUseCase<Any, Any, InferenceInfo>
): AndroidViewModel(application) {

    private val _inferenceInfo: MutableLiveData<InferenceInfo> by lazy {
        MutableLiveData(InferenceInfo())
    }
    val inferenceInfo: LiveData<InferenceInfo> = _inferenceInfo

    private val _output: MutableLiveData<Any?> = MutableLiveData(null)
    val output: LiveData<Any?> = _output

    val settingsChanges by lazy {
        useCase.getInferenceSettings().prefsChanges.asLiveData()
    }

    init {
        viewModelScope.launch {
            val settings = useCase.getInferenceSettings()
            settings.prefsChanges.collect {
                val changePrefName = it.prefKey

                useCase.applySettingsChange(changePrefName, settings)
            }
        }
    }

    @Synchronized
    open fun performUseCase(input: Any): Any? {
        val results = useCase.runModels(getApplication(), input)

        _inferenceInfo.postValue(results.second)
        _output.postValue(results.first)

        return output
    }

    override fun onCleared() {
        useCase.destroyModels()
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