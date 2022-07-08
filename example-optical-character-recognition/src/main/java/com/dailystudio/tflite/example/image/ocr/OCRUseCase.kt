package com.dailystudio.tflite.example.image.ocr

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.tflite.example.image.ocr.model.OCRDetectionModel
import com.dailystudio.tflite.example.image.ocr.model.OCRRecognitionModel
import org.tensorflow.lite.examples.ocr.RecognitionResult
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.AvgTime
import com.dailystudio.tensorflow.litex.LiteModel
import com.dailystudio.tensorflow.litex.fragment.ImageLiteUseCase
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs

class OCRUseCase: ImageLiteUseCase<RecognitionResult, OCRInferenceInfo>() {

    companion object {
        const val UC_NAME = "ocr"
    }

    private val avgDetectionTime = AvgTime(20)
    private val avgRecognitionTime = AvgTime(20)

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: OCRInferenceInfo): RecognitionResult? {
        val detectionModel = (defaultModel as? OCRDetectionModel) ?: return null
        val recognitionModel = (liteModels?.get(1) as? OCRRecognitionModel) ?: return null

        val ocrResults: HashMap<String, Int> = HashMap()

        val start = System.currentTimeMillis()
        val detectionResult = detectionModel.detectTexts(inferenceBitmap)
        val detectionEnd = System.currentTimeMillis()
        info.detectionTime = (System.currentTimeMillis() - start)

        val bitmapWithBoundingBoxes = if (detectionResult != null) {
            val bitmap = recognitionModel.recognizeTexts(
                inferenceBitmap, detectionResult, ocrResults
            )

            info.recognitionTime = (System.currentTimeMillis() - detectionEnd)

            bitmap
        } else {
            info.recognitionTime = 0L

            inferenceBitmap
        }

        if (useAverageTime) {
            avgDetectionTime.record(info.detectionTime)
            avgRecognitionTime.record(info.recognitionTime)

            info.detectionTime = avgDetectionTime.value
            info.recognitionTime = avgRecognitionTime.value
        }

        return RecognitionResult(bitmapWithBoundingBoxes,
            "OCR result", ocrResults).apply {
            this.detectionTime = info.detectionTime
            this.recognitionTime = info.recognitionTime
        }
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

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            OCRDetectionModel(context, device, numOfThreads, useXNNPack),
            OCRRecognitionModel(context, numOfThreads),
        )
    }


}