package com.dailystudio.tflite.example.common.image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Environment
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ImageUtils.toBitmap
import com.dailystudio.tflite.example.common.InferenceAgent
import java.io.File

abstract class AbsImageAnalyzer<Info: ImageInferenceInfo, Results> (private val rotation: Int,
                                                                    private val lensFacing: Int): ImageAnalysis.Analyzer {

    private var inferenceAgent: InferenceAgent<Info, Results> =
        InferenceAgent()

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        var results: Results? = null
        val info: Info = createInferenceInfo().apply {
            imageSize = Size(image.width, image.height)
            imageRotation = image.imageInfo.rotationDegrees
            cameraLensFacing = lensFacing
            screenRotation = rotation
        }

        val start = System.currentTimeMillis()
        image.image?.let {
            val frameBitmap: Bitmap? = it.toBitmap()
            val inferenceBitmap: Bitmap? = preProcessImage(frameBitmap, info)

            inferenceBitmap?.let { bitmap ->
                info.inferenceImageSize = Size(bitmap.width, bitmap.height)

                results = analyzeFrame(bitmap, info)
            }
        }
        val end = System.currentTimeMillis()

        info.analysisTime = (end - start)
        if (info.inferenceTime == 0L) {
            info.inferenceTime = info.analysisTime
        }

        Logger.debug("analysis [in ${info.analysisTime} ms (inference: ${info.inferenceTime} ms)]: result = ${results.toString().replace("%", "%%")}")

        inferenceAgent.deliverInferenceInfo(info)

        image.close()

        results?.let {
            inferenceAgent.deliverResults(it)
        }
    }

    protected open fun preProcessImage(frameBitmap: Bitmap?,
                                       info: Info): Bitmap? {
        return frameBitmap
    }

    protected open fun setResultsUpdateInterval(interval: Long) {
        inferenceAgent.resultsUpdateInterval = interval
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

        ImageUtils.saveBitmap(bitmap, File(dir, filename))
    }

    protected open fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    abstract fun createInferenceInfo(): Info

    abstract fun analyzeFrame(inferenceBitmap: Bitmap,
                              info: Info): Results?

}