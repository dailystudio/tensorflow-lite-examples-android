package com.dailystudio.tflite.example.image.detection.fragment

import com.dailystudio.tflite.example.image.detection.DetectorUseCase
import com.dailystudio.tensorflow.litex.fragment.LiteCameraUseCaseFragment


class ObjectDetectionCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(DetectorUseCase.UC_NAME)

}