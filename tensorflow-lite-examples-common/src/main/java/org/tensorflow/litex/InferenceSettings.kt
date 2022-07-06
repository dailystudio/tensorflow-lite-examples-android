package org.tensorflow.litex

import org.tensorflow.lite.support.model.Model.Device

class InferenceSettings(val device: Device,
                        val numOfThreads: Int = 1,
                        val useXNNPack: Boolean = true,
) {

    override fun toString(): String {
        return buildString {
            append("device: $device, ")
            append("numOfThreads: $numOfThreads, ")
            append("useXNNPack: $useXNNPack")
        }
    }

}