package com.dailystudio.tflite.example.transfer.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.TFLiteModel
import org.tensorflow.litex.images.Recognition

class TransferLearningModel(
    context: Context,
    device: Model.Device,
    numOfThreads: Int,
): LiteMultipleSignatureModel(context, MODEL_PATH, device, numOfThreads, NUM_OF_CLASSES) {

    companion object {
        const val MODEL_PATH = "model.tflite"
        const val NUM_OF_CLASSES = 4
    }

    fun analyze(bitmap: Bitmap):List<Recognition>? {
        return null
    }

}