package com.dailystudio.tflite.example.speech.recognition

import android.content.Context
import com.dailystudio.devbricksx.development.Logger
import org.tensorflow.lite.examples.speech.CommandRecognizer
import org.tensorflow.lite.examples.speech.RecognizeCommands
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.LiteModel
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs
import java.util.*

class SpeechRecognitionUseCase: LiteUseCase<ShortArray, RecognizeCommands.RecognitionResult, AudioInferenceInfo>() {

    companion object {
        const val UC_NAME = "speechrecognition"
    }

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            CommandRecognizer(context, device, numOfThreads, useXNNPack)
        )
    }

    override fun createInferenceInfo(): AudioInferenceInfo {
        return AudioInferenceInfo()
    }

    override fun runInference(
        input: ShortArray,
        info: AudioInferenceInfo
    ): RecognizeCommands.RecognitionResult? {
        val floatInputBuffer = Array(CommandRecognizer.RECORDING_LENGTH) { FloatArray(1) }

        val startTime = Date().time

        // We need to feed in float values between -1.0f and 1.0f, so divide the
        // signed 16-bit inputs.
        for (i in 0 until CommandRecognizer.RECORDING_LENGTH) {
            floatInputBuffer[i][0] = input[i] / 32767.0f
        }

        val inferenceStartTime = Date().time
        // Run the model.
        val result = try {
            (defaultModel as? CommandRecognizer)?.recognizeCommand(floatInputBuffer)
        } catch (e: Exception) {
            Logger.error("inference failed: $e")
            null
        }

        Logger.debug("result: $result")

        val endTime = Date().time

        info.sampleRate = CommandRecognizer.SAMPLE_RATE
        info.bufferSize = input.size
        info.inferenceTime = endTime - inferenceStartTime
        info.analysisTime = endTime - startTime

        return result
    }
}