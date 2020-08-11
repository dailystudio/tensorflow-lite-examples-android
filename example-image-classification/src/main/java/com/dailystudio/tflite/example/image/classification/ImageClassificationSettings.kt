package com.dailystudio.tflite.example.image.classification

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference
import com.dailystudio.tflite.example.common.ui.InferenceSettings
import org.tensorflow.lite.support.model.Model

@SharedPreference
class ImageClassificationSettings(@PreferenceValue
                                  val tfLiteModel: String? = null,
                                  device: String = Model.Device.CPU.toString(),
                                  numOfThread: Int = 1)
    : InferenceSettings(device, numOfThread)