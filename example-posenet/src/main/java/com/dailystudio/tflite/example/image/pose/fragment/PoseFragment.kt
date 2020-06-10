package com.dailystudio.tflite.example.image.pose.fragment

import android.graphics.Bitmap
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo

class PoseAnalyzer(rotation: Int) : AbsExampleAnalyzer<InferenceInfo, Void>(rotation) {

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: InferenceInfo): Void? {
        return null
    }

}

class PoseFragment : AbsExampleFragment<InferenceInfo, Void>() {

    override fun createAnalyzer(screenAspectRatio: Int,
                                rotation: Int): AbsExampleAnalyzer<InferenceInfo, Void> {
        return PoseAnalyzer(rotation)
    }

}