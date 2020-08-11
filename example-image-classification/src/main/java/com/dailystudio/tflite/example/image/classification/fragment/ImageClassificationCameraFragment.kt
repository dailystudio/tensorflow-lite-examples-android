package com.dailystudio.tflite.example.image.classification.fragment

import android.graphics.Bitmap
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.preference.AbsPrefs
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.classification.ImageClassificationSettingsPrefs
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.examples.classification.tflite.Classifier.Recognition
import org.tensorflow.lite.support.model.Model
import java.lang.Exception

private class ImageClassificationAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<ImageInferenceInfo, List<Recognition>>(rotation, lensFacing) {

    private var classifier: Classifier? = null

    @Synchronized
    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): List<Recognition>? {
        var results: List<Recognition>? = null

        if (classifier == null) {
            val context = GlobalContextWrapper.context
            context?.let {
                val modelStr = ImageClassificationSettingsPrefs.instance.tfLiteModel
                val deviceStr = ImageClassificationSettingsPrefs.instance.device

                val model = modelStr?.let {str ->
                    try {
                        Classifier.Model.valueOf(str)
                    } catch (e: Exception) {
                        Logger.warn("cannot parse model from [$str]: $e")

                        Classifier.Model.QUANTIZED_MOBILENET
                    }
                } ?: Classifier.Model.QUANTIZED_MOBILENET

                val device = try {
                        Model.Device.valueOf(deviceStr)
                } catch (e: Exception) {
                    Logger.warn("cannot parse device from [$deviceStr]: $e")

                    Model.Device.CPU
                }

                val threads = ImageClassificationSettingsPrefs.instance.numberOfThreads
                Logger.debug("[CLF UPDATE]: classifier creating: model = $model, device = $device, threads = $threads")


                classifier = Classifier.create(it,
                    model, device, threads)
            }

            Logger.debug("[CLF UPDATE]: classifier created =  $classifier")
            Logger.debug("classifier created: $classifier")
        }

        classifier?.let { classifier ->
            val start = System.currentTimeMillis()
            results = classifier.recognizeImage(inferenceBitmap, info.imageRotation)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
        }

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
       return ImageInferenceInfo()
    }

    override fun onInferenceSettingsChange(changePrefName: String) {
        super.onInferenceSettingsChange(changePrefName)
        Logger.debug("[CLF UPDATE]: new settings: $changePrefName")

        when (changePrefName) {
            ImageClassificationSettingsPrefs.PREF_TF_LITE_MODEL,
            InferenceSettingsPrefs.PREF_DEVICE,
            InferenceSettingsPrefs.PREF_NUMBER_OF_THREADS -> invalidateClassifier()
        }
    }

    @Synchronized
    private fun invalidateClassifier() {
        classifier?.close()
        classifier = null
        Logger.debug("[CLF UPDATE]: classifier is invalidated to null")
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, 640, 480, 0, true)

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix)
    }

}

class ImageClassificationCameraFragment : AbsExampleCameraFragment<ImageInferenceInfo, List<Recognition>>() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int): AbsImageAnalyzer<ImageInferenceInfo, List<Recognition>> {
        return ImageClassificationAnalyzer(rotation, lensFacing)
    }

    override fun getSettingsPreference(): AbsPrefs {
        return ImageClassificationSettingsPrefs.instance
    }

}