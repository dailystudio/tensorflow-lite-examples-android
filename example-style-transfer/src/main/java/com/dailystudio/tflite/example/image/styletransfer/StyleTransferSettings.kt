package com.dailystudio.tflite.example.image.styletransfer

import com.dailystudio.devbricksx.annotations.data.BooleanField
import com.dailystudio.devbricksx.annotations.data.DataStoreCompanion
import com.dailystudio.devbricksx.annotations.data.StringField
import com.dailystudio.tensorflow.litex.ui.InferenceSettings
import org.tensorflow.lite.examples.styletransfer.FSTModel
import org.tensorflow.lite.support.model.Model

@DataStoreCompanion
class StyleTransferSettings(@StringField("FastStyleTransferInt8")
                            val tfLiteModel: String = FSTModel.FastStyleTransferInt8.toString(),
                            @BooleanField(DEFAULT_REUSE_PREDICT)
                            val reusePredict: Boolean = DEFAULT_REUSE_PREDICT,
                            device: String = Model.Device.CPU.toString(),
                            numOfThread: Int = 1,
                            userAverageTime: Boolean = true,
) : InferenceSettings(device, numOfThread, userAverageTime) {

    companion object {
        const val DEFAULT_REUSE_PREDICT = true
    }

}