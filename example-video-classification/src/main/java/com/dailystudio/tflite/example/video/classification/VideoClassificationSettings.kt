package com.dailystudio.tflite.example.video.classification

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference
import com.dailystudio.tflite.example.common.ui.InferenceSettings
import org.tensorflow.lite.examples.videoclassification.ml.VideoClassifier
import org.tensorflow.lite.support.model.Model

@SharedPreference
class VideoClassificationSettings(@PreferenceValue(defaultValueStr = "MOVINET_A0")
                                  val classifierModel: String = VideoClassifier.ClassifierModel.MOVINET_A0.toString(),
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