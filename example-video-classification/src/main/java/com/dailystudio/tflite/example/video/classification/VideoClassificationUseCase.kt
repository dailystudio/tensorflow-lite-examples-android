package com.dailystudio.tflite.example.video.classification

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import org.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.examples.videoclassification.ml.VideoClassifier
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.fragment.ImageLiteUseCase
import java.lang.Exception

class VideoClassificationUseCase: ImageLiteUseCase<List<Category>, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "videoclassifier"

        private const val PRE_SCALE_WIDTH = 640
        private const val PRE_SCALE_HEIGHT = 480

        private const val ORIGINAL_IMAGE_FILE = "vc-pre-scaled.png"
        private const val PRE_SCALED_IMAGE_FILE = "vc-pre-scaled.png"

        const val MODEL_FPS = 5 // Ensure the input images are fed to the model at this fps.
        const val MAX_CAPTURE_FPS = 20
        private const val MODEL_FPS_ERROR_RANGE = 0.1 // Acceptable error range in fps.

    }

    private var lastResults: List<Category>? = null
    private var lastInferenceStartTime: Long = 0

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: ImageInferenceInfo): List<Category>? {
        var results: List<Category>? = lastResults

        val currentTime = SystemClock.uptimeMillis()
        val diff = currentTime - lastInferenceStartTime

        // Check to ensure that we only run inference at a frequency required by the
        // model, within an acceptable error range (e.g. 10%). Discard the frames
        // that comes too early.
        if (diff * MODEL_FPS >= 1000 /* milliseconds */ * (1 - MODEL_FPS_ERROR_RANGE)) {
            val start = System.currentTimeMillis()
            results = (defaultModel as? VideoClassifier)?.classify(inferenceBitmap)
            val end = System.currentTimeMillis()

            info.inferenceTime = (end - start)
            if (lastInferenceStartTime == 0L) {
                info.analysisTime = info.inferenceTime
            } else {
                info.analysisTime = currentTime - lastInferenceStartTime + info.inferenceTime
            }

            lastResults = results
            lastInferenceStartTime = currentTime
        }

        return results
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun applySettingsChange(
        changePrefName: String,
        inferenceSettings: InferenceSettingsPrefs
    ) {
        super.applySettingsChange(changePrefName, inferenceSettings)

        Logger.debug("new settings: $changePrefName")

        when (changePrefName) {
            VideoClassificationSettingsPrefs.PREF_CLASSIFIER_MODEL -> invalidateModels()
        }
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo
    ): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        dumpIntermediateBitmap(frameBitmap,  ORIGINAL_IMAGE_FILE)

        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width, frameBitmap.height,
            PRE_SCALE_WIDTH, PRE_SCALE_HEIGHT, info.imageRotation, true)

        val scaledBitmap = ImageUtils.createTransformedBitmap(frameBitmap, matrix)

        dumpIntermediateBitmap(scaledBitmap,  PRE_SCALED_IMAGE_FILE)

        return scaledBitmap
    }

    override fun invalidateModels() {
        super.invalidateModels()

        lastInferenceStartTime = 0L
    }

    @Synchronized
    fun resetVideoState() {
        synchronized(lockOfModels) {
            (defaultModel as? VideoClassifier)?.reset()
        }
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        val modelStr = if (settings is VideoClassificationSettingsPrefs) {
            settings.classifierModel
        } else {
            VideoClassifier.ClassifierModel.MOVINET_A0.toString()
        }

        val model = try {
            VideoClassifier.ClassifierModel.valueOf(modelStr)
        } catch (e: Exception) {
            Logger.warn("cannot parse model from [$modelStr]: $e")

            VideoClassifier.ClassifierModel.MOVINET_A0
        }

        return arrayOf(VideoClassifier.create(context,
            model, device, numOfThreads, useXNNPack))
    }

    override fun getInferenceSettings(): InferenceSettingsPrefs {
        return VideoClassificationSettingsPrefs.instance
    }

}