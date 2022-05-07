package com.dailystudio.tflite.example.transfer.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.TFLiteModel
import org.tensorflow.litex.images.Recognition

class OndeviceLearningModel(
    context: Context,
    modelPath: String,
    device: Model.Device,
    numOfThreads: Int
): TFLiteModel(context, modelPath, device, numOfThreads) {

    fun analyze(bitmap: Bitmap):List<Recognition>? {
        return null
    }

}