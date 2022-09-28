package com.dailystudio.tflite.example.video.classification

import com.dailystudio.devbricksx.annotations.data.BooleanField
import com.dailystudio.devbricksx.annotations.data.DataStoreCompanion
import com.dailystudio.devbricksx.annotations.data.StringField
import com.dailystudio.tensorflow.litex.ui.InferenceSettings
import org.tensorflow.lite.examples.videoclassification.ml.VideoClassifier
import org.tensorflow.lite.support.model.Model

@DataStoreCompanion
class VideoClassificationSettings(@StringField("MOVINET_A0")
                                  val classifierModel: String = VideoClassifier.ClassifierModel.MOVINET_A0.toString(),
                                  @BooleanField(IMAGE_PRE_SCALE_ENABLED)
                                  val enableImagePreScale: Boolean = true,
                                  device: String = Model.Device.CPU.toString(),
                                  numOfThread: Int = 1,
                                  userAverageTime: Boolean = true,
) : InferenceSettings(device, numOfThread, userAverageTime) {

    companion object {
        const val IMAGE_PRE_SCALE_ENABLED = true
    }
}