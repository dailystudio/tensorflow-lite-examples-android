package com.dailystudio.tflite.example.common.image

import android.graphics.Bitmap
import android.os.Environment
import android.util.Size
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ImageUtils.toBitmap
import com.dailystudio.tflite.example.common.AbsTFLiteModelRunner
import org.tensorflow.litex.TFLiteModel
import java.io.File

abstract class AbsImageAnalyzer<Model: TFLiteModel, Info: ImageInferenceInfo, Results> (private val rotation: Int,
                                                                                        private val lensFacing: Int,
                                                                                        useAverageTime: Boolean = true,
): AbsTFLiteModelRunner<Model, ImageProxy, Info, Results>(useAverageTime) {

    override fun analyze(model: Model, data: ImageProxy, info: Info): Results? {
        var results: Results? = null
        info.apply {
            imageSize = Size(data.width, data.height)
            imageRotation = data.imageInfo.rotationDegrees
            cameraLensFacing = lensFacing
            screenRotation = rotation
        }

        data.image?.let {
            val frameBitmap: Bitmap? = it.toBitmap()
            val inferenceBitmap: Bitmap? = preProcessImage(frameBitmap, info)

            inferenceBitmap?.let { bitmap ->
                info.inferenceImageSize = Size(bitmap.width, bitmap.height)

                results = analyzeFrame(model, bitmap, info)
            }
        }

        data.close()

        return results
    }

    protected open fun preProcessImage(frameBitmap: Bitmap?,
                                       info: Info): Bitmap? {
        return frameBitmap
    }

    protected fun dumpIntermediateBitmap(bitmap: Bitmap,
                                         filename: String) {
        if (!isDumpIntermediatesEnabled()) {
            return
        }

        saveIntermediateBitmap(bitmap, filename)
    }

    protected fun saveIntermediateBitmap(bitmap: Bitmap,
                                         filename: String) {
        val dir = GlobalContextWrapper.context?.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )

        val file = File(dir, filename)
        val ret = ImageUtils.saveBitmap(bitmap, file)
        Logger.debug("intermediate saved [$ret] in [${file.absolutePath}]")
    }

    protected open fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    abstract fun analyzeFrame(model: Model,
                              inferenceBitmap: Bitmap,
                              info: Info): Results?

}