package com.dailystudio.tflite.example.image.classification

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference
import com.dailystudio.tflite.example.common.ui.InferenceSettings
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.support.model.Model

@SharedPreference
class ImageClassificationSettings(@PreferenceValue(defaultValueStr = "QUANTIZED_MOBILENET")
                                  val tfLiteModel: String = Classifier.Model.QUANTIZED_MOBILENET.toString(),
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