package com.dailystudio.tflite.example.image.segmentation.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Size
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AdvanceInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.segmentation.SegmentationUseCase
import org.tensorflow.lite.examples.imagesegmentation.ImageSegmentationModelExecutor
import org.tensorflow.lite.examples.imagesegmentation.SegmentationResult
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class ImageSegmentationCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(
            SegmentationUseCase.UC_NAME
        )

}