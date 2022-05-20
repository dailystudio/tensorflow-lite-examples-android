package com.dailystudio.tflite.example.image.classification.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.classification.ImageClassificationSettingsPrefs
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.images.Recognition
import java.lang.Exception

private class ImageClassificationAnalyzer(rotation: Int,
                                          lensFacing: Int,
                                          useAverageTime: Boolean,
) : AbsImageAnalyzer<Classifier, ImageInferenceInfo, List<Recognition>>(rotation, lensFacing, useAverageTime) {

    override fun analyzeFrame(
        model: Classifier,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Recognition>? {
        var results: List<Recognition>? = null

        val start = System.currentTimeMillis()
        results = model.recognizeImage(inferenceBitmap, info.imageRotation)
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
       return ImageInferenceInfo()
    }

    override fun onInferenceSettingsChange(
        changePrefName: String,
        inferenceSettings: InferenceSettingsPrefs
    ) {
        super.onInferenceSettingsChange(changePrefName, inferenceSettings)
        Logger.debug("new settings: $changePrefName")

        when (changePrefName) {
            ImageClassificationSettingsPrefs.PREF_TF_LITE_MODEL -> invalidateModel()
        }
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
        if (frameBitmap == null
            || !ImageClassificationSettingsPrefs.instance.enableImagePreScale) {
            return frameBitmap
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, 640, 480, 0, true)

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix)
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Classifier? {
        val modelStr = if (settings is ImageClassificationSettingsPrefs) {
            settings.tfLiteModel
        } else {
            Classifier.Model.QUANTIZED_MOBILENET.toString()
        }

        val model = modelStr?.let {str ->
            try {
                Classifier.Model.valueOf(str)
            } catch (e: Exception) {
                Logger.warn("cannot parse model from [$str]: $e")

                Classifier.Model.QUANTIZED_MOBILENET
            }
        } ?: Classifier.Model.QUANTIZED_MOBILENET

        return Classifier.create(context,
            model, device, numOfThreads, useXNNPack)
    }

}

class ImageClassificationCameraFragment : AbsExampleCameraFragment<Classifier, ImageInferenceInfo, List<Recognition>>() {

    override fun getSettingsPreference(): InferenceSettingsPrefs {
        return ImageClassificationSettingsPrefs.instance
    }

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<Classifier, ImageInferenceInfo, List<Recognition>> {
        return ImageClassificationAnalyzer(rotation, lensFacing, useAverageTime)
    }

}