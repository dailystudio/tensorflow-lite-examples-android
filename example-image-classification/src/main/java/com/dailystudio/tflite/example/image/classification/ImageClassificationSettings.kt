package com.dailystudio.tflite.example.image.classification

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference
import com.dailystudio.tflite.example.common.ui.InferenceSettings
import org.tensorflow.lite.support.model.Model

@SharedPreference
class ImageClassificationSettings(@PreferenceValue
                                  val tfLiteModel: String? = null,
                                  @PreferenceValue(defaultValueStr = IMAGE_PRE_SCALE_ENABLED.toString())
                                  val enableImagePreScale: Boolean = true,
                                  device: String = Model.Device.CPU.toString(),
                                  numOfThread: Int = 1,
                                  userAverageTime: Boolean = true,
) : InferenceSettings(device, numOfThread, userAverageTime) {

    companion object {
        const val IMAGE_PRE_SCALE_ENABLED = true
    }
}