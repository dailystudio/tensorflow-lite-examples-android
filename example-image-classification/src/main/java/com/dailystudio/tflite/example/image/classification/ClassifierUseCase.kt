package com.dailystudio.tflite.example.image.classification

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tensorflow.litex.image.ImageInferenceInfo
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.LiteModel
import com.dailystudio.tensorflow.litex.fragment.ImageLiteUseCase
import com.dailystudio.tensorflow.litex.image.Recognition
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs
import java.lang.Exception

class ClassifierUseCase: ImageLiteUseCase<List<Recognition>, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "classifier"
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        val modelStr = if (settings is ImageClassificationSettingsPrefs) {
            settings.tfLiteModel
        } else {
            Classifier.Model.QUANTIZED_MOBILENET.toString()
        }

        val model = try {
            Classifier.Model.valueOf(modelStr)
        } catch (e: Exception) {
            Logger.warn("cannot parse model from [$modelStr]: $e")

            Classifier.Model.QUANTIZED_MOBILENET
        }

        return arrayOf(Classifier.create(context,
            model, device, numOfThreads, useXNNPack))
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun getInferenceSettings(): InferenceSettingsPrefs {
        return ImageClassificationSettingsPrefs.instance
    }

    override fun analyzeFrame(
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Recognition>? {
        val start = System.currentTimeMillis()
        val results = (defaultModel as? Classifier)?.recognizeImage(
            inferenceBitmap, info.imageRotation)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        return results
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo
    ): Bitmap? {
        if (frameBitmap == null
            || !ImageClassificationSettingsPrefs.instance.enableImagePreScale) {
            return frameBitmap
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, 640, 480, 0, true)

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix)
    }

    override fun applySettingsChange(
        changePrefName: String,
        inferenceSettings: InferenceSettingsPrefs
    ) {
        super.applySettingsChange(changePrefName, inferenceSettings)
        Logger.debug("new settings: $changePrefName")

        when (changePrefName) {
            ImageClassificationSettingsPrefs.PREF_TF_LITE_MODEL -> invalidateModels()
        }
    }
}