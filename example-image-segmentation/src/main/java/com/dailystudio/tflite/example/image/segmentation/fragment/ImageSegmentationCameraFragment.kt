package com.dailystudio.tflite.example.image.segmentation.fragment

import com.dailystudio.tflite.example.image.segmentation.SegmentationUseCase
import org.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class ImageSegmentationCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(
            SegmentationUseCase.UC_NAME
        )

}