package com.dailystudio.tflite.example.image.styletransfer

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference
import com.dailystudio.tflite.example.common.ui.InferenceSettings
import org.tensorflow.lite.examples.styletransfer.FSTModel
import org.tensorflow.lite.support.model.Model

@SharedPreference
class StyleTransferSettings(@PreferenceValue(defaultValueStr = "FastStyleTransferInt8")
                            val tfLiteModel: String = FSTModel.FastStyleTransferInt8.toString(),
                            device: String = Model.Device.CPU.toString(),
                            numOfThread: Int = 1,
                            userAverageTime: Boolean = true,
) : InferenceSettings(device, numOfThread, userAverageTime)