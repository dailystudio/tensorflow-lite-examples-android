package com.dailystudio.tensorflow.litex.ui

import com.dailystudio.devbricksx.annotations.data.BooleanField
import com.dailystudio.devbricksx.annotations.data.DataStoreCompanion
import com.dailystudio.devbricksx.annotations.data.IntegerField
import com.dailystudio.devbricksx.annotations.data.StringField
import org.tensorflow.lite.support.model.Model

@DataStoreCompanion
open class InferenceSettings(@StringField(DEFAULT_DEVICE)
                             val device: String = DEFAULT_DEVICE,
                             @IntegerField(DEFAULT_NUM_OF_THREADS)
                             val numberOfThreads: Int = DEFAULT_NUM_OF_THREADS,
                             @BooleanField(USE_AVERAGE_TIME)
                             val useAverageTime: Boolean = USE_AVERAGE_TIME,
                             @BooleanField(USE_XNNPACK)
                             val useXNNPack: Boolean = USE_XNNPACK,
) {

    companion object {
        const val DEFAULT_DEVICE = "CPU"
        const val DEFAULT_NUM_OF_THREADS = 1
        const val MIN_NUM_OF_THREADS = 1
        const val MAX_NUM_OF_THREADS = 4
        const val NUM_OF_THREADS_STEP = 1
        const val USE_AVERAGE_TIME = true
        const val USE_XNNPACK = true
    }

}