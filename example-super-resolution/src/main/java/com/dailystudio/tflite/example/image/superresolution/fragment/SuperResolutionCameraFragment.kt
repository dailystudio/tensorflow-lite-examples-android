package com.dailystudio.tflite.example.image.superresolution.fragment

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.ImageAnalysis
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.superresolution.R
import com.dailystudio.tflite.example.image.superresolution.model.SuperResolutionModel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition


private class SuperResolutionAnalyzer(rotation: Int,
                                      lensFacing: Int,
                                      useAverageTime: Boolean,
) : AbsImageAnalyzer<SuperResolutionModel, ImageInferenceInfo, List<Recognition>>(rotation, lensFacing, useAverageTime) {

    companion object {
        const val TF_MODEL_PATH = "ESRGAN.tflite"
    }

    override fun analyzeFrame(
        model: SuperResolutionModel,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Recognition>? {
        var results: List<Recognition>? = null

        val start = System.currentTimeMillis()
        results = model.analyze(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        settings: InferenceSettingsPrefs
    ): SuperResolutionModel? {
        return SuperResolutionModel(context,
            TF_MODEL_PATH, device, numOfThreads)
    }

}

class SuperResolutionCameraFragment : AbsExampleCameraFragment<SuperResolutionModel, ImageInferenceInfo, List<Recognition>>() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example_super_resolution
    }

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<SuperResolutionModel, ImageInferenceInfo, List<Recognition>> {
        return SuperResolutionAnalyzer(rotation, lensFacing, useAverageTime)
    }

}