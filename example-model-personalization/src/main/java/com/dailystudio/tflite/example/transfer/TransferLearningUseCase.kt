package com.dailystudio.tflite.example.transfer

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import org.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.transfer.model.TransferLearningModel
import org.tensorflow.lite.examples.transfer.TransferLearningModelWrapper
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.fragment.ImageLiteUseCase

class TransferLearningUseCase: ImageLiteUseCase<Array<TransferLearningModel.Prediction>, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "transferlearning"
    }

    private val addSampleRequests = ArrayDeque<String>()

    fun addSample(sampleClass: String) {
        addSampleRequests.add(sampleClass)
    }

    fun enableTraining(lossConsumer: TransferLearningModel.LossConsumer) {
        (defaultModel as? TransferLearningModelWrapper)?.enableTraining(lossConsumer)
    }

    fun disableTraining() {
        (defaultModel as? TransferLearningModelWrapper)?.disableTraining()
    }

    override fun analyzeFrame(
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
            results = (defaultModel as? TransferLearningModelWrapper)?.predict(data)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
        } else {
            val data = TransferLearningModel.prepareCameraImage(
                inferenceBitmap, info.screenRotation)
            (defaultModel as? TransferLearningModelWrapper)?.addSample(data, sampleClass)

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

                Logger.debug("COLLECT: $info")

                ClassTrainingInfoManager.add(classInfo)
            }
        }

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            TransferLearningModelWrapper(context, device, numOfThreads, false)
        )
    }

}