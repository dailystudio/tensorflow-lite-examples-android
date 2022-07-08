package com.dailystudio.tflite.example.speech.recognition

import android.content.Context
import com.dailystudio.tensorflow.litex.InferenceInfo
import com.dailystudio.tensorflow.litex.InferenceInfoItem

class AudioInferenceInfo(var sampleRate: Int = 0,
                         var bufferSize: Int = 0,
                         analysisTime: Long = 0,
                         inferenceTime: Long = 0) : InferenceInfo(analysisTime, inferenceTime) {

    override fun toInfoItems(context: Context): MutableList<InferenceInfoItem> {
        val items = super.toInfoItems(context)

        val idStart = items.size

        val resources = context.resources

        val itemSampleRate = InferenceInfoItem(idStart+ 1, R.drawable.ic_info_sample_rate,
            resources.getString(R.string.label_info_sample_rate), "%d Hz".format(sampleRate))
        items.add(itemSampleRate)

        val itemBufferSize = InferenceInfoItem(idStart + 2, R.drawable.ic_info_buffer_size,
            resources.getString(R.string.label_info_buffer_size), "%dB".format(bufferSize))
        items.add(itemBufferSize)

        return items
    }

    override fun toString(): String {
        return buildString {
            append("sample rate: $sampleRate,")
            append(super.toString())
        }
    }

}