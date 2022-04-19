package com.dailystudio.tflite.example.common.image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Environment
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.preference.AbsPrefs
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ImageUtils.toBitmap
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import java.io.File

class AvgTime(private val capacity: Int = 10) {

    private val timeValues = Array<Long>(capacity) { 0 }
    private var wIndex = 0

    val value: Long
        get() {
            val len = timeValues.size
            var sum = 0L

            for (i in 0 until len) {
                sum += timeValues[i]
            }
            return sum / len
        }


    fun record(newValue: Long) {
        timeValues[wIndex] = newValue
        wIndex = (wIndex + 1) % capacity
    }

}


abstract class AbsImageAnalyzer<Info: ImageInferenceInfo, Results> (private val rotation: Int,
                                                                    private val lensFacing: Int,
                                                                    var useAverageTime: Boolean = true,
                                                                    var preprocessEnabled: Boolean = true,
): ImageAnalysis.Analyzer {

    private var inferenceAgent: InferenceAgent<Info, Results> =
        InferenceAgent()

    private val avgInferenceTime = AvgTime(20)
    private val avgAnalyzeTime = AvgTime(20)

    init {
        inferenceAgent.deliverInferenceInfo(createInferenceInfo())
    }

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
            val inferenceBitmap: Bitmap? = if (preprocessEnabled) {
                preProcessImage(frameBitmap, info)
            } else {
                frameBitmap
            }

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

        Logger.debug("useAverageTime = $useAverageTime")
        if (useAverageTime) {
            avgInferenceTime.record(info.inferenceTime)
            avgAnalyzeTime.record(info.analysisTime)

            info.inferenceTime = avgInferenceTime.value
            info.analysisTime = avgAnalyzeTime.value
        }

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

    @Synchronized
    open fun onInferenceSettingsChange(changePrefName: String, inferenceSettings: AbsPrefs) {
        Logger.debug("[SETTINGS UPDATE]: changed preference: $changePrefName")
        if (inferenceSettings !is InferenceSettingsPrefs) {
            return
        }

        when (changePrefName) {
            InferenceSettingsPrefs.PREF_ENABLE_IMAGE_PREPROCESS -> {
                preprocessEnabled = inferenceSettings.enableImagePreprocess
            }

            InferenceSettingsPrefs.PREF_USER_AVERAGE_TIME -> {
                useAverageTime = inferenceSettings.userAverageTime
            }
        }
    }

    abstract fun createInferenceInfo(): Info

    abstract fun analyzeFrame(inferenceBitmap: Bitmap,
                              info: Info): Results?

}