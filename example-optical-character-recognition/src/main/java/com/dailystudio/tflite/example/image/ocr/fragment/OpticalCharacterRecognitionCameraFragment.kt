package com.dailystudio.tflite.example.image.ocr.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.tflite.example.common.AvgTime
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.ocr.OCRInferenceInfo
import com.dailystudio.tflite.example.image.ocr.model.OpticalCharacterRecognitionModel
import org.tensorflow.lite.examples.ocr.ModelExecutionResult
import org.tensorflow.lite.support.model.Model


private class OpticalCharacterRecognitionAnalyzer(rotation: Int,
                                      lensFacing: Int,
                                      useAverageTime: Boolean,
) : AbsImageAnalyzer<OpticalCharacterRecognitionModel, OCRInferenceInfo, ModelExecutionResult>(rotation, lensFacing, useAverageTime) {

    private val avgDetectionTime = AvgTime(20)
    private val avgRecognitionTime = AvgTime(20)

    override fun analyzeFrame(
        model: OpticalCharacterRecognitionModel,
        inferenceBitmap: Bitmap,
        info: OCRInferenceInfo
    ): ModelExecutionResult? {
        var results: ModelExecutionResult? = null

        val start = System.currentTimeMillis()
        results = model.analyze(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        results?.let {
            info.detectionTime = if (it.detectionTime == 0L) {
                info.inferenceTime
            } else {
                it.detectionTime
            }

            info.recognitionTime = if (it.recognitionTime == 0L) {
                info.inferenceTime
            } else {
                it.recognitionTime
            }
        }

        if (useAverageTime) {
            avgDetectionTime.record(info.detectionTime)
            avgRecognitionTime.record(info.recognitionTime)

            info.detectionTime = avgDetectionTime.value
            info.recognitionTime = avgRecognitionTime.value

        }

        return results
    }

    override fun preProcessImage(frameBitmap: Bitmap?, info: OCRInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        return com.dailystudio.devbricksx.utils.ImageUtils.rotateBitmap(frameBitmap,
            info.imageRotation)
    }

    override fun createInferenceInfo(): OCRInferenceInfo {
        return OCRInferenceInfo()
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): OpticalCharacterRecognitionModel? {
        return OpticalCharacterRecognitionModel(context, device, numOfThreads, useXNNPack)
    }

}

class OpticalCharacterRecognitionCameraFragment
    : AbsExampleCameraFragment<OpticalCharacterRecognitionModel, OCRInferenceInfo, ModelExecutionResult>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<OpticalCharacterRecognitionModel, OCRInferenceInfo, ModelExecutionResult> {
        return OpticalCharacterRecognitionAnalyzer(rotation, lensFacing, useAverageTime)
    }

}