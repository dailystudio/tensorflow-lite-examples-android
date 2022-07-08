package com.dailystudio.tflite.example.image.ocr.fragment

import com.dailystudio.tflite.example.image.ocr.OCRUseCase
import com.dailystudio.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class OpticalCharacterRecognitionCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(OCRUseCase.UC_NAME)

}