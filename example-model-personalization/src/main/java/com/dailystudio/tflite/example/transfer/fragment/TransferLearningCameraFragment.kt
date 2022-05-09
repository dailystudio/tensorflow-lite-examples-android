package com.dailystudio.tflite.example.transfer.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.transfer.model.TransferLearningModel
import org.tensorflow.lite.examples.transfer.TransferLearningModelWrapper
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition
import kotlin.collections.ArrayDeque

private class TransferLearningAnalyzer(
    rotation: Int,
    lensFacing: Int,
    useAverageTime: Boolean,
) : AbsImageAnalyzer<TransferLearningModelWrapper, ImageInferenceInfo, Array<TransferLearningModel.Prediction>>(rotation, lensFacing, useAverageTime) {

    companion object {
    }

    private val addSampleRequests = ArrayDeque<String>()

    fun addSample(sampleClass: String) {
        addSampleRequests.add(sampleClass)
    }

    fun enableTraining(lossConsumer: TransferLearningModel.LossConsumer) {
        model?.enableTraining(lossConsumer)
    }

    fun disableTraining() {
        model?.disableTraining()
    }

    override fun analyzeFrame(
        model: TransferLearningModelWrapper,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): Array<TransferLearningModel.Prediction>? {
        var results: Array<TransferLearningModel.Prediction>? = null

        val sampleClass = addSampleRequests.removeFirstOrNull()
        Logger.debug("[ANA TRACK]: sampleClass = $sampleClass")
        if (sampleClass.isNullOrEmpty()) {
            Logger.debug("[ANA TRACK]: do analysis")
            val data = TransferLearningModel.prepareCameraImage(
                inferenceBitmap, info.screenRotation)

            // perform analysis
            val start = System.currentTimeMillis()
            results = model.predict(data)
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
    ): TransferLearningModelWrapper? {
        return TransferLearningModelWrapper(context, device, numOfThreads)
    }

}

class TransferLearningCameraFragment : AbsExampleCameraFragment<TransferLearningModelWrapper, ImageInferenceInfo, Array<TransferLearningModel.Prediction>>() {

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<TransferLearningModelWrapper, ImageInferenceInfo, Array<TransferLearningModel.Prediction>> {
        return TransferLearningAnalyzer(rotation, lensFacing, useAverageTime)
    }

    fun addSample(sampleClass: String) {
        (analyzer as? TransferLearningAnalyzer)?.addSample(sampleClass)
    }

    fun enableTraining(lossConsumer: TransferLearningModel.LossConsumer) {
        (analyzer as? TransferLearningAnalyzer)?.enableTraining(lossConsumer)
    }

    fun disableTraining() {
        (analyzer as? TransferLearningAnalyzer)?.disableTraining()
    }

}