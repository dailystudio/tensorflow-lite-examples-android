package com.dailystudio.tflite.example.common

import org.tensorflow.lite.support.model.Model.Device

class InferenceSettings(val device: Device,
                        val numOfThreads: Int = 1) {

    override fun toString(): String {
        return buildString {
            append("device: $device, ")
            append("numOfThreads: $numOfThreads")
        }
    }

}