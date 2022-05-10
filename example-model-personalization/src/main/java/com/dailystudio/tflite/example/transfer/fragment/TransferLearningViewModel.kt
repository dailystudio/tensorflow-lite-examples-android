package com.dailystudio.tflite.example.transfer.fragment

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.transfer.ClassTrainingInfo
import com.dailystudio.tflite.example.transfer.model.ClassTrainingInfoViewModel

class TransferLearningViewModel(application: Application): ClassTrainingInfoViewModel(application) {
    /**
     * Current state of training.
     */
    enum class TrainingState {
        NOT_STARTED, STARTED, PAUSED
    }

    enum class Mode {
        Capture,
        Inference
    }

    private val _trainingState = MutableLiveData(TrainingState.NOT_STARTED)
    val trainingState = _trainingState

    private val _mode = MutableLiveData(Mode.Capture)
    val mode = _mode

    fun addSample(sampleClass: String) {
        val info = getClassTrainingInfo(sampleClass) ?: ClassTrainingInfo(sampleClass)

        info.numOfSamples++

        updateClassTrainingInfo(info)
    }

    fun changeModeTo(mode: Mode) {
        _mode.postValue(mode)
    }

}