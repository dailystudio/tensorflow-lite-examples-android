package com.dailystudio.tflite.example.image.detection.fragment

import android.graphics.Bitmap
import android.util.Size
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleAnalyzer
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo

private class ObjectDetectionAnalyzer(rotation: Int) : AbsExampleAnalyzer<InferenceInfo, Void>(rotation) {

    override fun analyzeFrame(frameBitmap: Bitmap, info: InferenceInfo): Void? {
        return null
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }

    override fun getDesiredImageResolution(): Size? {
        return Size(640, 360)
    }

}

class ObjectDetectionFragment : AbsExampleFragment<InferenceInfo, Void>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int)
            : AbsExampleAnalyzer<InferenceInfo, Void> {
        return ObjectDetectionAnalyzer(rotation)
    }

}