package com.dailystudio.tflite.example.common

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ImageUtils.toBitmap

interface AnalysisResultsCallback<Results> {

    fun onResult(results: Results)

}

abstract class AbsExampleAnalyzer<Results> (private val rotation: Int): ImageAnalysis.Analyzer {

    private val callbacks: MutableList<AnalysisResultsCallback<Results>> = mutableListOf()

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        Logger.debug("image dimen: ${image.width} x ${image.height}")
        Logger.debug("image rotation: ${image.imageInfo.rotationDegrees}")
        Logger.debug("screen rotation: $rotation")

        var result: Results? = null

        val start = System.currentTimeMillis()
        image.image?.let {
            var frameBitmap: Bitmap? = it.toBitmap()
            val desiredSize = getDesiredImageResolution()
            if (desiredSize != null) {
                frameBitmap = ImageUtils.scaleBitmapRatioLocked(frameBitmap,
                    desiredSize.width, desiredSize.height)
            }

            frameBitmap?.let { bitmap ->
                result = analyzeFrame(bitmap, rotation)
            }
        }
        val end = System.currentTimeMillis()

        Logger.debug("analysis [in ${end - start} ms]: result = $result")

        image.close()

        result?.let {
            for (c in callbacks) {
                c.onResult(it)
            }
        }
    }

    fun addCallback(callback: AnalysisResultsCallback<Results>) {
        callbacks.add(callback)
    }

    protected open fun getDesiredImageResolution(): Size? {
        return null
    }

    abstract fun analyzeFrame(frameBitmap: Bitmap,
                              rotation: Int): Results?

}