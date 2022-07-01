package com.dailystudio.tflite.example.image.pose.fragment

import com.dailystudio.tflite.example.image.pose.PoseUseCase
import org.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class PoseCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(PoseUseCase.UC_NAME)

}