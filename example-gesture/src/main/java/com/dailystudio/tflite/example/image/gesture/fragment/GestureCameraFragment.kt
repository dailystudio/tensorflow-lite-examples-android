package com.dailystudio.tflite.example.image.gesture.fragment

import androidx.camera.core.CameraSelector
import com.dailystudio.tflite.example.image.gesture.GestureUseCase
import org.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class GestureCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(
            GestureUseCase.UC_NAME
        )

    override fun getDefaultCameraLens(): Int {
        return CameraSelector.LENS_FACING_FRONT
    }

}