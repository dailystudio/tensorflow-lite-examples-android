package com.dailystudio.tflite.example.template.fragment

import com.dailystudio.tflite.example.template.ExampleUseCase
import org.tensorflow.litex.fragment.LiteCameraUseCaseFragment


class ExampleTemplateCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(ExampleUseCase.UC_NAME)

}