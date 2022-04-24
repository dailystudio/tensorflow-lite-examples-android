package com.dailystudio.tflite.example.video.classification.fragment

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.examples.videoclassification.ml.VideoClassifier
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model

private class VideoClassificationAnalyzer(rotation: Int,
                                          lensFacing: Int,
                                          useAverageTime: Boolean,
) : AbsImageAnalyzer<VideoClassifier, ImageInferenceInfo, List<Category>>(rotation, lensFacing, useAverageTime) {

    companion object {
        private const val PRE_SCALE_WIDTH = 640
        private const val PRE_SCALE_HEIGHT = 480

        private const val ORIGINAL_IMAGE_FILE = "vc-pre-scaled.png"
        private const val PRE_SCALED_IMAGE_FILE = "vc-pre-scaled.png"
    }

    override fun analyzeFrame(
        model: VideoClassifier,
        inferenceBitmap: Bitmap,
        info: ImageInferenceInfo
    ): List<Category>? {
        var results: List<Category>? = null

        val start = System.currentTimeMillis()
        saveIntermediateBitmap(inferenceBitmap, "vc-infer.png")
        results = model.classify(inferenceBitmap, info.imageRotation)
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
//            ImageClassificationSettingsPrefs.PREF_TF_LITE_MODEL -> invalidateModel()
        }
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: ImageInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
//            || !ImageClassificationSettingsPrefs.instance.enableImagePreScale) {
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
        settings: InferenceSettingsPrefs
    ): VideoClassifier? {
        val model = VideoClassifier.ClassifierModel.MOVINET_A0

        return VideoClassifier.create(context,
            model, device, numOfThreads)
    }

    @Synchronized
    fun resetVideoState() {
        model?.reset()
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return true
    }

}

class VideoClassificationCameraFragment : AbsExampleCameraFragment<VideoClassifier, ImageInferenceInfo, List<Category>>() {

    override fun getSettingsPreference(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
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