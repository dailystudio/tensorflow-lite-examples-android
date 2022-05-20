package com.dailystudio.tflite.example.speech.recognition.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.dailystudio.devbricksx.audio.AudioConfig
import com.dailystudio.devbricksx.audio.AudioProcessFragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.speech.recognition.AudioInferenceInfo
import com.dailystudio.tflite.example.speech.recognition.R
import org.tensorflow.lite.examples.speech.RecognizeCommands.RecognitionResult
import org.tensorflow.lite.examples.speech.CommandRecognizer
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.MLUseCase
import java.util.*

class SpeechRecognitionUseCase(lifecycleOwner: LifecycleOwner,
                               useAverageTime: Boolean
): MLUseCase<CommandRecognizer, ShortArray, RecognitionResult, AudioInferenceInfo>(lifecycleOwner, useAverageTime) {

    override fun runInference(
        model: CommandRecognizer,
        data: ShortArray,
        info: AudioInferenceInfo
    ): RecognitionResult? {
        val floatInputBuffer = Array(CommandRecognizer.RECORDING_LENGTH) { FloatArray(1) }

        val startTime = Date().time

        // We need to feed in float values between -1.0f and 1.0f, so divide the
        // signed 16-bit inputs.
        for (i in 0 until CommandRecognizer.RECORDING_LENGTH) {
            floatInputBuffer[i][0] = data[i] / 32767.0f
        }

        val inferenceStartTime = Date().time
        // Run the model.
        val result = try {
            model.recognizeCommand(floatInputBuffer)
        } catch (e: Exception) {
            Logger.error("inference failed: $e")
            null
        }

        Logger.debug("result: $result")

        val endTime = Date().time

        info.sampleRate = CommandRecognizer.SAMPLE_RATE
        info.bufferSize = data.size
        info.inferenceTime = endTime - inferenceStartTime
        info.analysisTime = endTime - startTime

        return result
    }

    override fun getSettingsPreference(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

    override fun createModel(
        context: Context,
        device: Model.Device,
        threads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): CommandRecognizer? {
        return CommandRecognizer(context, Model.Device.CPU, 4, true)
    }

    override fun createInferenceInfo(): AudioInferenceInfo {
        return AudioInferenceInfo()
    }

}
class SpeechRecognitionFragment : AudioProcessFragment() {

    private var useCase: SpeechRecognitionUseCase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useCase = SpeechRecognitionUseCase(this, true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_speech_recognition, container, false)

    override fun onProcessAudioData(audioConfig: AudioConfig, audioData: ShortArray) {
        useCase?.run(audioData)
    }

}