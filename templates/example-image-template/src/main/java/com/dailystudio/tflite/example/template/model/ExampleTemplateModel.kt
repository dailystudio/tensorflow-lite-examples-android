package com.dailystudio.tflite.example.template.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.AssetFileLiteModel

class ExampleTemplateModel(
    context: Context,
    modelPath: String,
    device: Model.Device,
    numOfThreads: Int
): AssetFileLiteModel(context, modelPath, device, numOfThreads) {

    fun analyze(bitmap: Bitmap): Void? {
        return null
    }

}