package com.dailystudio.tflite.example.transfer.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.transfer.ClassTrainingInfoManager
import com.dailystudio.tflite.example.transfer.R
import com.dailystudio.tflite.example.transfer.model.TransferLearningModel
import org.tensorflow.lite.examples.transfer.TransferLearningModelWrapper
import org.tensorflow.lite.support.model.Model
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
            val data = TransferLearningModel.prepareCameraImage(
                inferenceBitmap, info.screenRotation)
            model.addSample(data, sampleClass)

            val classInfo = ClassTrainingInfoManager.get(sampleClass)
            classInfo?.let {
                val context = GlobalContextWrapper.context ?: return@let

                val iconSize = context.resources.getDimensionPixelSize(R.dimen.class_button_icon_size)
                val matrix = MatrixUtils.getTransformationMatrix(
                    inferenceBitmap.width, inferenceBitmap.height,
                    iconSize, iconSize,
                    info.imageRotation,
                    maintainAspectRatio = true,
                    fitIn = false
                )
                val scaled = ImageUtils.createTransformedBitmap(inferenceBitmap, matrix)
                it.lastSample = scaled

                ClassTrainingInfoManager.add(classInfo)
            }
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

    private lateinit var viewModel: TransferLearningViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransferLearningViewModel::class.java]
    }

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