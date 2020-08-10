package com.dailystudio.tflite.example.common.ui

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference
import org.tensorflow.lite.support.model.Model

@SharedPreference
class BaseSetting(@PreferenceValue(defaultValueStr = "CPU") val device: String = Model.Device.CPU.toString(),
                  @PreferenceValue(defaultValueStr = "1") val numberOfThreads: Int = 1) {
    companion object {

        const val DEFAULT_NUM_OF_THREADS = 1
        const val MIN_NUM_OF_THREADS = 1
        const val MAX_NUM_OF_THREADS = 4
        const val NUM_OF_THREADS_STEP = 1
    }
}