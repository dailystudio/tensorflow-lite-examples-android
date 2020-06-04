package com.dailystudio.tflite.example.common

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ImageUtils.toBitmap

interface ResultsCallback<Results> {

    fun onResult(results: Results)

}

interface InferenceCallback<Info: InferenceInfo> {

    fun onInference(info: Info)

}

open class InferenceInfo(var imageSize: Size = Size(0, 0),
                         var imageRotation: Int = 0,
                         var screenRotation: Int = 0,
                         var inferenceImageSize: Size = Size(0, 0),
                         var inferenceTime: Long = 0) {

    override fun toString(): String {
        return buildString {
            append("image size: $imageSize,")
            append("image rotation: $imageRotation,")
            append("screen rotation: $screenRotation,")
            append("inference size: $inferenceImageSize,")
            append("inference time: $inferenceTime")
        }
    }

}

abstract class AbsExampleAnalyzer<Info: InferenceInfo, Results> (private val rotation: Int): ImageAnalysis.Analyzer {

    private val resultsCallbacks: MutableList<ResultsCallback<Results>> = mutableListOf()
    private val inferenceCallbacks: MutableList<InferenceCallback<Info>> = mutableListOf()

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        var result: Results? = null
        var info: Info = createInferenceInfo().apply {
            imageSize = Size(image.width, image.height)
            imageRotation = image.imageInfo.rotationDegrees
            screenRotation = rotation
        }

        val start = System.currentTimeMillis()
        image.image?.let {
            var frameBitmap: Bitmap? = it.toBitmap()
            val desiredSize = getDesiredImageResolution()
            if (desiredSize != null) {
                frameBitmap = ImageUtils.scaleBitmapRatioLocked(frameBitmap,
                    desiredSize.width, desiredSize.height)
            }

            frameBitmap?.let { bitmap ->
                info.inferenceImageSize = Size(bitmap.width, bitmap.height)

                result = analyzeFrame(bitmap, info)
            }
        }
        val end = System.currentTimeMillis()

        info.inferenceTime = (end - start)
        Logger.debug("analysis [in ${info.inferenceTime} ms]: result = $result")

        for (c in inferenceCallbacks) {
            c.onInference(info)
        }

        image.close()

        result?.let {
            for (c in resultsCallbacks) {
                c.onResult(it)
            }
        }
    }

    fun addResultCallback(callback: ResultsCallback<Results>) {
        resultsCallbacks.add(callback)
    }

    fun addInferenceCallback(callback: InferenceCallback<Info>) {
        inferenceCallbacks.add(callback)
    }

    protected open fun getDesiredImageResolution(): Size? {
        return null
    }

    abstract fun createInferenceInfo(): Info

    abstract fun analyzeFrame(frameBitmap: Bitmap,
                              info: Info): Results?


}