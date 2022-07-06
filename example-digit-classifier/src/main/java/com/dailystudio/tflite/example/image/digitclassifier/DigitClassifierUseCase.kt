package com.dailystudio.tflite.example.image.digitclassifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.util.Size
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import org.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.image.digitclassifier.fragment.RecognizedDigit
import org.tensorflow.lite.examples.digitclassifier.DigitClassifier
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.LiteModel
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.ui.InferenceSettingsPrefs
import java.io.File

class DigitClassifierUseCase: LiteUseCase<Bitmap, RecognizedDigit, ImageInferenceInfo>() {

    companion object {
        const val UC_NAME = "digitclassifier"

        private const val PRE_SCALE_SIZE = (28 * 3)
        private const val INFERENCE_IMAGE_FILE = "inference.png"
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(DigitClassifier(context, device, numOfThreads, useXNNPack))
    }

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }

    override fun runInference(input: Bitmap, info: ImageInferenceInfo): RecognizedDigit? {
        val classifier = (defaultModel as? DigitClassifier) ?: return null

        info.imageSize = Size(input.width, input.height)
        info.inferenceImageSize = classifier?.getInferenceSize() ?: Size (0, 0)

        val start = System.currentTimeMillis()
        val inferenceBitmap = preProcessBitmap(input, info)
        dumpIntermediateBitmap(inferenceBitmap, INFERENCE_IMAGE_FILE)

        val inferenceStart = System.currentTimeMillis()
        val result = classifier.classify(inferenceBitmap)
        val end = System.currentTimeMillis()

        info.inferenceTime = end - inferenceStart
        info.analysisTime = end - start

        return RecognizedDigit(inferenceBitmap,
            result?.first ?: -1,
            result?.second ?: 0f
        )
    }

    private fun preProcessBitmap(frameBitmap: Bitmap,
                                 info: ImageInferenceInfo
    ): Bitmap {
        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width,  frameBitmap.height,
            PRE_SCALE_SIZE, PRE_SCALE_SIZE,
            0,
            maintainAspectRatio = true,
            fitIn = true)

        val paddingColor = Color.BLACK

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix, paddingColor = paddingColor)
    }

    private fun dumpIntermediateBitmap(bitmap: Bitmap,
                                       filename: String) {
        if (!isDumpIntermediatesEnabled()) {
            return
        }

        saveIntermediateBitmap(bitmap, filename)
    }

    private fun saveIntermediateBitmap(bitmap: Bitmap,
                                       filename: String) {
        val dir = GlobalContextWrapper.context?.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )

        ImageUtils.saveBitmap(bitmap, File(dir, filename))
    }

    private fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

}