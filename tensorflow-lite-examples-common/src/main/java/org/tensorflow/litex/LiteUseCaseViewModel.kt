package org.tensorflow.litex

import android.app.Application
import androidx.lifecycle.*
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class LiteUseCaseViewModel<Input, Output, Info: InferenceInfo>(
    application: Application
): AndroidViewModel(application) {

    private var useCase: LiteUseCase<Input, Output, Info>? = null

    private val _inferenceInfo: MutableLiveData<Info> by lazy {
        MutableLiveData(createInferenceInfo())
    }

    private val inferenceInfo: LiveData<Info> = _inferenceInfo

    private val _output: MutableLiveData<Output?> = MutableLiveData(null)
    private val output: LiveData<Output?> = _output

    private val settingsChanges by lazy {
        getSettingsPreference().prefsChanges.asLiveData()
    }

    init {
        viewModelScope.launch {
            getSettingsPreference().prefsChanges.collect {
                val changePrefName = it.prefKey
                val settings = getSettingsPreference()
                Logger.debug("[WATCH CHANGE]: changed preference: $changePrefName")

                when (changePrefName) {
                    InferenceSettingsPrefs.PREF_DEVICE,
                    InferenceSettingsPrefs.PREF_NUMBER_OF_THREADS,
                    InferenceSettingsPrefs.PREF_USE_X_N_N_PACK -> {
                        invalidateUseCase()
                    }
                }

                onInferenceSettingsChange(it.prefKey, settings)
            }
        }
    }

    @Synchronized
    open fun performUseCase(data: Input): Output? {
        if (useCase == null) {
            useCase = createUseCase(getSettingsPreference())?.apply {
                prepare()
            }

        }

        val info = createInferenceInfo()

        return useCase?.perform(data, info).also { output ->
            _inferenceInfo.postValue(info)
            _output.postValue(output)
        }
    }

    @Synchronized
    protected open fun invalidateUseCase() {
        Logger.debug("[USE_CASE_CREATION]: model is invalidated")
        destroyUseCase()

        useCase = null
    }

    open fun destroyUseCase() {
        useCase?.destroy()
    }

    override fun onCleared() {
        destroyUseCase()
    }

    protected open fun onInferenceSettingsChange(changePrefName: String, inferenceSettings: InferenceSettingsPrefs) {
    }

    abstract fun createUseCase(settings: InferenceSettingsPrefs): LiteUseCase<Input, Output, Info>?
    abstract fun createInferenceInfo(): Info
    abstract fun getSettingsPreference(): InferenceSettingsPrefs

}