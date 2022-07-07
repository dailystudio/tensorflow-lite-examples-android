package com.dailystudio.tflite.example.image.styletransfer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Size
import androidx.camera.core.CameraSelector
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import org.tensorflow.litex.image.AdvanceInferenceInfo
import org.tensorflow.lite.examples.styletransfer.*
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.fragment.ImageLiteUseCase
import org.tensorflow.litex.ui.InferenceSettingsPrefs

class StyleTransferUseCase: ImageLiteUseCase<StyleTransferResult, AdvanceInferenceInfo>() {

    companion object {
        const val UC_NAME = "styletransfer"

        private const val CONTENT_IMAGE_SIZE = 384

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val STYLED_IMAGE_FILE = "styled.png"
    }

    private var styleBitmap: Bitmap? = null
    private var styleName: String? = null

    private var styleBottleneck: StyleBottleneck = PredictModelExecutor.EMPTY_BOTTLENECK

    private var reusePrediction: Boolean = StyleTransferSettings.DEFAULT_REUSE_PREDICT

    private val predictModel: PredictModelExecutor?
        get() {
            return liteModels?.getOrNull(0) as? PredictModelExecutor
        }

    private val transformModel: TransformModelExecutor?
        get() {
            return liteModels?.getOrNull(1) as? TransformModelExecutor
        }

    fun selectStyle(styleName: String) {
        styleBitmap = null
        this.styleName = styleName
    }

    override fun analyzeFrame(
        inferenceBitmap: Bitmap,
        info: AdvanceInferenceInfo
    ): StyleTransferResult? {
        val context = GlobalContextWrapper.context ?: return null

        var inferenceTime = 0L

        var results: StyleTransferResult? = null

        if (styleName == null) {
            styleName = StyleTransferPrefs.getSelectedStyle(context)
        }

        val skipPredict = if (styleBitmap == null) {
            styleBitmap = ImageUtils.loadAssetBitmap(context,
                "thumbnails/${this.styleName}")
            false
        } else {
            true
        }

        val doStylePrediction = (!reusePrediction || !skipPredict)
        if (doStylePrediction) {
            styleBottleneck = styleBitmap?.let {
                val predictResult = predictModel?.execute(it)
                if (predictResult != null) {
                    inferenceTime += predictResult.stylePredictTime
                    info.preProcessTime = predictResult.preProcessTime
                }
                predictResult?.styleBottleneck
            } ?: PredictModelExecutor.EMPTY_BOTTLENECK
        }

        val styledBitmap = transformModel?.execute(inferenceBitmap, styleBottleneck)?.let {
            inferenceTime += it.styleTransferTime

            info.flattenTime = it.postProcessTime

            it.styledImage
        } ?: inferenceBitmap

        val trimmed = ImageUtils.trimBitmap(
            styledBitmap, info.frameSize.width.toFloat() / info.frameSize.height)
        results = StyleTransferResult(trimmed)
        dumpIntermediateBitmap(trimmed, STYLED_IMAGE_FILE)

        return results
    }

    override fun createInferenceInfo(): AdvanceInferenceInfo {
        return AdvanceInferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: AdvanceInferenceInfo
    ): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        if (info.imageRotation % 90 == 0) {
            info.frameSize = Size(frameBitmap.height, frameBitmap.width)
        } else {
            info.frameSize = Size(frameBitmap.width, frameBitmap.height)
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE,
            info.imageRotation, true, fitIn = true)

        val preScaledBitmap = ImageUtils.createTransformedBitmap(frameBitmap,
            matrix, paddingColor = Color.BLACK)

        val flipped = if (info.cameraLensFacing == CameraSelector.LENS_FACING_FRONT) {
            ImageUtils.flipBitmap(preScaledBitmap)
        } else {
            preScaledBitmap
        }

        dumpIntermediateBitmap(flipped, PRE_SCALED_IMAGE_FILE)

        return flipped
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    override fun applySettingsChange(
        changePrefName: String,
        inferenceSettings: InferenceSettingsPrefs
    ) {
        super.applySettingsChange(changePrefName, inferenceSettings)
        Logger.debug("new settings: $changePrefName")

        when (changePrefName) {
            StyleTransferSettingsPrefs.PREF_TF_LITE_MODEL -> invalidateModels()
            StyleTransferSettingsPrefs.PREF_REUSE_PREDICT -> {
                if (inferenceSettings is StyleTransferSettingsPrefs) {
                    reusePrediction = inferenceSettings.reusePredict
                }
            }
        }
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        val liteModelStr = if (settings is StyleTransferSettingsPrefs) {
            settings.tfLiteModel
        } else {
            FSTModel.FastStyleTransferInt8.toString()
        }

        val liteModel =  try {
            FSTModel.valueOf(liteModelStr)
        } catch (e: Exception) {
            Logger.warn("cannot parse TensorFlow Lite model from [$liteModelStr]: $e")

            FSTModel.FastStyleTransferInt8
        }

        val predictModelPath = if (liteModel == FSTModel.FastStyleTransferFloat16) {
            PredictModelExecutor.STYLE_PREDICT_FLOAT16_MODEL
        } else {
            PredictModelExecutor.STYLE_PREDICT_INT8_MODEL
        }

        val transferModelPath = if (liteModel == FSTModel.FastStyleTransferFloat16) {
            TransformModelExecutor.STYLE_TRANSFER_FLOAT16_MODEL
        } else {
            TransformModelExecutor.STYLE_TRANSFER_INT8_MODEL
        }

        return arrayOf(
            PredictModelExecutor(context, predictModelPath, device, numOfThreads, useXNNPack),
            TransformModelExecutor(context, transferModelPath, device, numOfThreads, useXNNPack)
        )
    }

    override fun getInferenceSettings(): InferenceSettingsPrefs {
        return StyleTransferSettingsPrefs.instance
    }

}