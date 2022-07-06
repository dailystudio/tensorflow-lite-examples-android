package com.dailystudio.tflite.example.image.ocr

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import org.tensorflow.litex.InferenceInfoItem
import org.tensorflow.litex.image.ImageInferenceInfo

class OCRInferenceInfo(imageSize: Size = Size(0, 0),
                       imageRotation: Int = 0,
                       screenRotation: Int = 0,
                       cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK,
                       inferenceImageSize: Size = Size(0, 0),
                       analysisTime: Long = 0,
                       inferenceTime: Long = 0
) : ImageInferenceInfo(imageSize, imageRotation, screenRotation, cameraLensFacing, inferenceImageSize, analysisTime, inferenceTime) {

    var detectionTime: Long = 0L
    var recognitionTime: Long = 0L

    override fun toInfoItems(context: Context): MutableList<InferenceInfoItem> {
        val items = super.toInfoItems(context)

        val idStart = items.size

        val resources = context.resources

        val itemSampleRate = InferenceInfoItem(idStart+ 1, R.drawable.ic_info_detect_time,
            resources.getString(R.string.label_info_detection_time), "%d ms".format(detectionTime))
        items.add(itemSampleRate)

        val itemBufferSize = InferenceInfoItem(idStart + 2, R.drawable.ic_info_recognize_time,
            resources.getString(R.string.label_info_recognition_time), "%d ms".format(recognitionTime))
        items.add(itemBufferSize)

        return items
    }

    override fun toString(): String {
        return buildString {
            append("detection time: $detectionTime,")
            append("recognition time: $recognitionTime,")
            append(super.toString())
        }
    }

}