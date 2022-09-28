package com.dailystudio.tflite.example.image.classification

import com.dailystudio.devbricksx.annotations.data.BooleanField
import com.dailystudio.devbricksx.annotations.data.DataStoreCompanion
import com.dailystudio.devbricksx.annotations.data.StringField
import com.dailystudio.tensorflow.litex.ui.InferenceSettings
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.support.model.Model

@DataStoreCompanion
class ImageClassificationSettings(@StringField("QUANTIZED_MOBILENET")
                                  val tfLiteModel: String = Classifier.Model.QUANTIZED_MOBILENET.toString(),
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