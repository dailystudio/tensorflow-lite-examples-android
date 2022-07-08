package com.dailystudio.tflite.example.image.classification.fragment

import com.dailystudio.tflite.example.image.classification.ClassifierUseCase
import com.dailystudio.tensorflow.litex.fragment.LiteCameraUseCaseFragment

/*
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
*/

class ImageClassificationCameraFragment : LiteCameraUseCaseFragment() {
    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(ClassifierUseCase.UC_NAME)

}