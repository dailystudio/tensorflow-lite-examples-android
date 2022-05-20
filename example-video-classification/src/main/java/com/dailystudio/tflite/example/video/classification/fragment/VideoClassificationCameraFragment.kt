package com.dailystudio.tflite.example.video.classification.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CaptureRequest
import android.os.SystemClock
import android.util.Range
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.video.classification.VideoClassificationSettingsPrefs
import org.tensorflow.lite.examples.videoclassification.ml.VideoClassifier
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model
import java.lang.Exception

private class VideoClassificationAnalyzer(rotation: Int,
                                          lensFacing: Int,
                                          useAverageTime: Boolean,
) : AbsImageAnalyzer<VideoClassifier, ImageInferenceInfo, List<Category>>(rotation, lensFacing, useAverageTime) {

    companion object {
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

    override fun analyzeFrame(
        model: VideoClassifier,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Category>? {
        var results: List<Category>? = lastResults

        val currentTime = SystemClock.uptimeMillis()
        val diff = currentTime - lastInferenceStartTime

        // Check to ensure that we only run inference at a frequency required by the
        // model, within an acceptable error range (e.g. 10%). Discard the frames
        // that comes too early.
        if (diff * MODEL_FPS >= 1000 /* milliseconds */ * (1 - MODEL_FPS_ERROR_RANGE)) {
            val start = System.currentTimeMillis()
            results = model.classify(inferenceBitmap)
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

    override fun onInferenceSettingsChange(
        changePrefName: String,
        inferenceSettings: InferenceSettingsPrefs
    ) {
        super.onInferenceSettingsChange(changePrefName, inferenceSettings)
        Logger.debug("new settings: $changePrefName")

        when (changePrefName) {
            VideoClassificationSettingsPrefs.PREF_CLASSIFIER_MODEL -> invalidateModel()
        }
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
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

    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): VideoClassifier? {
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

        return VideoClassifier.create(context,
            model, device, numOfThreads, useXNNPack)
    }

    override fun invalidateModel() {
        super.invalidateModel()
        lastInferenceStartTime = 0L
    }

    @Synchronized
    fun resetVideoState() {
        model?.reset()
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

}

class VideoClassificationCameraFragment : AbsExampleCameraFragment<VideoClassifier, ImageInferenceInfo, List<Category>>() {

    override fun getImageAnalysisBuilder(
        screenAspectRatio: Int,
        rotation: Int
    ): ImageAnalysis.Builder {
        val builder = super.getImageAnalysisBuilder(screenAspectRatio, rotation)

        builder.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

        val captureFps = VideoClassificationAnalyzer.MAX_CAPTURE_FPS
        val modelFps = VideoClassificationAnalyzer.MODEL_FPS
        val targetFpsMultiplier = captureFps.div(modelFps)
        val targetCaptureFps = modelFps * targetFpsMultiplier

        val extender: Camera2Interop.Extender<*> =
            Camera2Interop.Extender(builder)
        extender.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
            Range(targetCaptureFps, targetCaptureFps)
        )

        return builder
    }

    override fun getSettingsPreference(): InferenceSettingsPrefs {
        return VideoClassificationSettingsPrefs.instance
    }

    override fun createAnalyzer(
        screenAspectRatio: Int,
        rotation: Int,
        lensFacing: Int,
        useAverageTime: Boolean,
    ): AbsImageAnalyzer<VideoClassifier, ImageInferenceInfo, List<Category>> {
        return VideoClassificationAnalyzer(rotation, lensFacing, useAverageTime)
    }

    fun resetModelState() {
        val analyzer = analyzer
        if (analyzer is VideoClassificationAnalyzer) {
            analyzer.resetVideoState()
        }
    }

}