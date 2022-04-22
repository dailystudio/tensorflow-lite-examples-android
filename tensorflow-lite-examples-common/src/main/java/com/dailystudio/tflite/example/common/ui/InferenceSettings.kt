package com.dailystudio.tflite.example.common.ui

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference
import org.tensorflow.lite.support.model.Model

@SharedPreference
open class InferenceSettings(@PreferenceValue(defaultValueStr = "CPU")
                             val device: String = Model.Device.CPU.toString(),
                             @PreferenceValue(defaultValueStr = DEFAULT_NUM_OF_THREADS.toString())
                             val numberOfThreads: Int = 1,
                             @PreferenceValue(defaultValueStr = USE_AVERAGE_TIME.toString())
                             val userAverageTime: Boolean = true,
) {

    companion object {
        const val DEFAULT_NUM_OF_THREADS = 1
        const val MIN_NUM_OF_THREADS = 1
        const val MAX_NUM_OF_THREADS = 4
        const val NUM_OF_THREADS_STEP = 1
        const val USE_AVERAGE_TIME = true
    }

}