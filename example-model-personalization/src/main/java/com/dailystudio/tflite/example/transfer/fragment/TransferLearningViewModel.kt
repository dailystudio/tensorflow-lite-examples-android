package com.dailystudio.tflite.example.transfer.fragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class TransferLearningViewModel(application: Application): AndroidViewModel(application) {
    /**
     * Current state of training.
     */
    enum class TrainingState {
        NOT_STARTED, STARTED, PAUSED
    }

    private val _trainingState = MutableLiveData(TrainingState.NOT_STARTED)
    private val trainingState = _trainingState
}