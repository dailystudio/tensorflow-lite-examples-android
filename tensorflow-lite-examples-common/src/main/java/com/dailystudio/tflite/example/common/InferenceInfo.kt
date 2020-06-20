package com.dailystudio.tflite.example.common

import android.content.Context
import com.dailystudio.devbricksx.inmemory.InMemoryObject

open class InferenceInfo(var analysisTime: Long = 0,
                         var inferenceTime: Long = 0) {

    open fun toInfoItems(context: Context): MutableList<InferenceInfoItem> {
        val items = mutableListOf<InferenceInfoItem>()

        val resources = context.resources

        val itemAnalysisTime = InferenceInfoItem(5, R.drawable.ic_info_analysis_time,
            resources.getString(R.string.label_info_analysis_time),
            "%d ms".format(analysisTime))
        items.add(itemAnalysisTime)

        val itemInferenceTime = InferenceInfoItem(6, R.drawable.ic_info_inference_time,
            resources.getString(R.string.label_info_inference_time),
            "%d ms".format(inferenceTime))
        items.add(itemInferenceTime)

        return items
    }

    override fun toString(): String {
        return buildString {
            append("analysis time: $analysisTime,")
            append("inference time: $inferenceTime")
        }
    }

}

class InferenceInfoItem(val id: Int,
                        val iconResId: Int,
                        val label: CharSequence,
                        val value: CharSequence) : InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other !is InferenceInfoItem) {
            return false
        }

        return (id == other.id)
                && (iconResId == other.iconResId)
                && (label == other.label)
                && (value == other.value)
    }

}
