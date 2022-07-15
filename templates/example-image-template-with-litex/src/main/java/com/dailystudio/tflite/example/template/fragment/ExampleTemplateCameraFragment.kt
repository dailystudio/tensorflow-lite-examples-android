package com.dailystudio.tflite.example.template.fragment

import com.dailystudio.tflite.example.template.ExampleTemplateUseCase
import com.dailystudio.tensorflow.litex.fragment.LiteCameraUseCaseFragment


class ExampleTemplateCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(ExampleTemplateUseCase.UC_NAME)

}