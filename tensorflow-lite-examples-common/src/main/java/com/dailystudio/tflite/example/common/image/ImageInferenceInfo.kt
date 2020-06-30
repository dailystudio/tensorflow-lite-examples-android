package com.dailystudio.tflite.example.common.image

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.InferenceInfoItem
import com.dailystudio.tflite.example.common.R

open class ImageInferenceInfo(var imageSize: Size = Size(0, 0),
                              var imageRotation: Int = 0,
                              var screenRotation: Int = 0,
                              var cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK,
                              var inferenceImageSize: Size = Size(0, 0),
                              analysisTime: Long = 0,
                              inferenceTime: Long = 0): InferenceInfo(analysisTime, inferenceTime) {

    override fun toInfoItems(context: Context): MutableList<InferenceInfoItem> {
        val items = super.toInfoItems(context)

        val idStart = items.size

        val resources = context.resources

        val itemImageSize = InferenceInfoItem(idStart + 1, R.drawable.ic_info_image_size,
            resources.getString(R.string.label_info_image_size), imageSize.toString())
        items.add(itemImageSize)

        val itemImageRotation = InferenceInfoItem(idStart + 2, R.drawable.ic_info_image_rotation,
            resources.getString(R.string.label_info_image_rotation), imageRotation.toString())
        items.add(itemImageRotation)

        val itemScreenRotation = InferenceInfoItem(idStart + 3, R.drawable.ic_info_screen_rotation,
            resources.getString(R.string.label_info_screen_rotation), screenRotation.toString())
        items.add(itemScreenRotation)

        val itemInferenceImageSize = InferenceInfoItem(idStart + 4, R.drawable.ic_info_inference_image_size,
            resources.getString(R.string.label_info_inference_image_size), inferenceImageSize.toString())
        items.add(itemInferenceImageSize)

        return items
    }

    override fun toString(): String {
        return buildString {
            append("image size: $imageSize,")
            append("image rotation: $imageRotation,")
            append("screen rotation: $screenRotation,")
            append("lens facing: $cameraLensFacing,")
            append("inference size: $inferenceImageSize,")
            append(super.toString())
        }
    }

}
