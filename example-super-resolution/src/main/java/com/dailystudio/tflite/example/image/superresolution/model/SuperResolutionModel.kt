package com.dailystudio.tflite.example.image.superresolution.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.TFLiteModel
import org.tensorflow.litex.images.Recognition

class SuperResolutionModel(
    context: Context,
    modelPath: String,
    device: Model.Device,
    numOfThreads: Int
): TFLiteModel(context, modelPath, device, numOfThreads) {

    fun analyze(bitmap: Bitmap): SuperRes? {
        return SuperRes(bitmap, bitmap)
    }

}