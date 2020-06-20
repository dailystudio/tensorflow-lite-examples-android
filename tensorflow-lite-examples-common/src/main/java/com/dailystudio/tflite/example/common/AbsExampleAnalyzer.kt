package com.dailystudio.tflite.example.common

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
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.call
import java.io.File
import kotlin.math.roundToLong

open class InferenceInfo(var imageSize: Size = Size(0, 0),
                         var imageRotation: Int = 0,
                         var screenRotation: Int = 0,
                         var inferenceImageSize: Size = Size(0, 0),
                         var analysisTime: Long = 0,
                         var inferenceTime: Long = 0) {

    override fun toString(): String {
        return buildString {
            append("image size: $imageSize,")
            append("image rotation: $imageRotation,")
            append("screen rotation: $screenRotation,")
            append("inference size: $inferenceImageSize,")
            append("analysis time: $analysisTime,")
            append("inference time: $inferenceTime")
        }
    }

}

abstract class AbsExampleAnalyzer<Info: InferenceInfo, Results> (private val rotation: Int): ImageAnalysis.Analyzer {

    private var lastDelivered: Long = -1

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        var results: Results? = null
        val info: Info = createInferenceInfo().apply {
            imageSize = Size(image.width, image.height)
            imageRotation = image.imageInfo.rotationDegrees
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

        deliverInferenceInfo(info)

        image.close()

        results?.let {
            deliverResults(it)
        }
    }

    private fun deliverInferenceInfo(info: InferenceInfo) {
        KDispatcher.call(Constants.EVENT_INFERENCE_INFO_UPDATE, info)
    }

    private fun deliverResults(results: Results) {
        val interval = getResultsUpdateInterval()
        Logger.debug("interval = $interval")

        if (interval <= 0L || lastDelivered == -1L) {
            triggerResultsCallbacks(results)
        } else {
            val now = System.currentTimeMillis()
            if (now - lastDelivered > interval) {
                triggerResultsCallbacks(results)
            } else {
                Logger.warn("skip results, since interval[${now - lastDelivered}] is less than $interval")
            }
        }
    }

    private fun triggerResultsCallbacks(results: Results) {
        KDispatcher.call(Constants.EVENT_RESULTS_UPDATE, results)

        lastDelivered = System.currentTimeMillis()
    }

    protected open fun preProcessImage(frameBitmap: Bitmap?,
                                       info: Info): Bitmap? {
        return frameBitmap
    }

    protected open fun getResultsUpdateInterval(): Long {
        return (1000 / 30f).roundToLong()
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