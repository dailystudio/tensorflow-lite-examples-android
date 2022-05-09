package com.dailystudio.tflite.example.transfer.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.transfer.model.TransferLearningModel
import org.tensorflow.lite.examples.transfer.TransferLearningModelWrapper
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition
import java.util.*
import kotlin.collections.ArrayDeque


private class TransferLearningAnalyzer(
    rotation: Int,
    lensFacing: Int,
    useAverageTime: Boolean,
) : AbsImageAnalyzer<TransferLearningModel, ImageInferenceInfo, List<Recognition>>(rotation, lensFacing, useAverageTime) {

    companion object {
    }

    private val addSampleRequests = ArrayDeque<String>()

    fun addSample(sampleClass: String) {
        addSampleRequests.add(sampleClass)
    }

    override fun analyzeFrame(
        model: TransferLearningModel,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Recognition>? {
        var results: List<Recognition>? = null

        val sampleClass = addSampleRequests.removeFirstOrNull()
        Logger.debug("[ANA TRACK]: sampleClass = $sampleClass")
        if (sampleClass.isNullOrEmpty()) {
            Logger.debug("[ANA TRACK]: do analysis")
            // perform analysis
            val start = System.currentTimeMillis()
            results = model.analyze(inferenceBitmap)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
        } else {
            Logger.debug("[ANA TRACK]: add sample for class $sampleClass")
            val data = TransferLearningModel.prepareCameraImage(
                inferenceBitmap, info.screenRotation)
            model.addSample(data, sampleClass)
        }

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
    ): TransferLearningModel? {
        return TransferLearningModel(context, device, numOfThreads,
            arrayListOf("1", "2", "3", "4")
        )
    }

}

class TransferLearningCameraFragment : AbsExampleCameraFragment<TransferLearningModel, ImageInferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<TransferLearningModel, ImageInferenceInfo, List<Recognition>> {
        return TransferLearningAnalyzer(rotation, lensFacing, useAverageTime)
    }

    fun addSamples(sampleClass: String) {
        (analyzer as? TransferLearningAnalyzer)?.addSample(sampleClass)
    }

}