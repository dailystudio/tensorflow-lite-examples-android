package com.dailystudio.tflite.example.image.ocr.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.ocr.model.OpticalCharacterRecognitionModel
import org.tensorflow.lite.examples.ocr.ImageUtils
import org.tensorflow.lite.examples.ocr.ModelExecutionResult
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition


private class OpticalCharacterRecognitionAnalyzer(rotation: Int,
                                      lensFacing: Int,
                                      useAverageTime: Boolean,
) : AbsImageAnalyzer<OpticalCharacterRecognitionModel, ImageInferenceInfo, ModelExecutionResult>(rotation, lensFacing, useAverageTime) {

    override fun analyzeFrame(
        model: OpticalCharacterRecognitionModel,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): ModelExecutionResult? {
        var results: ModelExecutionResult? = null

        val start = System.currentTimeMillis()
        results = model.analyze(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        return results
    }

    override fun preProcessImage(frameBitmap: Bitmap?, info: ImageInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        return com.dailystudio.devbricksx.utils.ImageUtils.rotateBitmap(frameBitmap,
            info.imageRotation)
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        settings: InferenceSettingsPrefs
    ): OpticalCharacterRecognitionModel? {
        return OpticalCharacterRecognitionModel(context, device, numOfThreads)
    }

}

class OpticalCharacterRecognitionCameraFragment
    : AbsExampleCameraFragment<OpticalCharacterRecognitionModel, ImageInferenceInfo, ModelExecutionResult>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<OpticalCharacterRecognitionModel, ImageInferenceInfo, ModelExecutionResult> {
        return OpticalCharacterRecognitionAnalyzer(rotation, lensFacing, useAverageTime)
    }

}