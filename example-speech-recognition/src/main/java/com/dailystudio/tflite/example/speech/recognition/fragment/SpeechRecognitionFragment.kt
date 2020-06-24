package com.dailystudio.tflite.example.speech.recognition.fragment

import android.Manifest
import android.content.res.AssetManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.async.ManagedThread
import com.dailystudio.devbricksx.audio.AudioConfig
import com.dailystudio.devbricksx.audio.AudioProcessFragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.AbsPermissionsFragment
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.speech.recognition.AudioInferenceInfo
import com.dailystudio.tflite.example.speech.recognition.R
import kotlinx.android.synthetic.main.fragment_speech_recognition.*
import kotlinx.coroutines.cancel
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.examples.speech.RecognizeCommands
import org.tensorflow.lite.examples.speech.RecognizeCommands.RecognitionResult
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max

class SpeechRecognitionFragment : AudioProcessFragment() {

    companion object {
        // you are running your own model.
        private const val SAMPLE_RATE = 16000
        private const val SAMPLE_DURATION_MS = 1000
        private const val RECORDING_LENGTH = (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000)
        private const val AVERAGE_WINDOW_DURATION_MS: Long = 1000
        private const val DETECTION_THRESHOLD = 0.50f
        private const val SUPPRESSION_MS = 1500
        private const val MINIMUM_COUNT = 3
        private const val MINIMUM_TIME_BETWEEN_SAMPLES_MS: Long = 30

        private const val LABEL_FILENAME = "conv_actions_labels.txt"
        private const val MODEL_FILENAME = "conv_actions_frozen.tflite"
    }

    private val labels: MutableList<String> = mutableListOf()
    private val displayedLabels: MutableList<String> = mutableListOf()
    private var recognizeCommands: RecognizeCommands? = null
    private var tfLite: Interpreter? = null
    private val tfliteOptions =
        Interpreter.Options()
    private var lastProcessingTimeMs: Long = 0

    private var inferenceAgent: InferenceAgent<InferenceInfo, RecognitionResult> =
        InferenceAgent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTflite()
    }

    private fun initTflite() {
        // Load the labels for the model, but only display those that don't start
        // with an underscore.
        val actualLabelFilename: String = LABEL_FILENAME
        Logger.debug("reading labels from: $actualLabelFilename")
        var br: BufferedReader? = null
        try {
            val stream = requireContext().assets.open(actualLabelFilename)
            Logger.debug("stream: $stream")
            val reader = InputStreamReader(stream)
            Logger.debug("reader: $reader")
            br = BufferedReader(reader)
            Logger.debug("br: $br")

            synchronized(labels) {
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    Logger.debug("line: $line")
                    line?.let {
                        labels.add(it)
                        if (it[0] != '_') {
                            displayedLabels.add(it.substring(0, 1).toUpperCase() + it.substring(1))
                        }
                    }
                }
            }
            br.close()
        } catch (e: IOException) {
            throw RuntimeException("Problem reading label file!", e)
        }

        // Set up an object to smooth recognition results to increase accuracy.

        // Set up an object to smooth recognition results to increase accuracy.
        recognizeCommands = RecognizeCommands(
            labels,
            AVERAGE_WINDOW_DURATION_MS,
            DETECTION_THRESHOLD,
            SUPPRESSION_MS,
            MINIMUM_COUNT,
            MINIMUM_TIME_BETWEEN_SAMPLES_MS
        )

        val actualModelFilename: String = MODEL_FILENAME
        try {
            val buffer = loadModelFile(requireContext().assets,
                actualModelFilename)

            buffer?.let {
                tfLite = Interpreter(it, tfliteOptions)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        tfLite?.resizeInput(0, intArrayOf(RECORDING_LENGTH, 1))
        tfLite?.resizeInput(1, intArrayOf(1))
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_speech_recognition, container, false)

    override fun onProcessAudioData(audioConfig: AudioConfig, audioData: ShortArray) {
        val inferenceInfo = AudioInferenceInfo()
        val floatInputBuffer = Array(RECORDING_LENGTH) { FloatArray(1) }
        val sampleRateList = intArrayOf(SAMPLE_RATE)

        val startTime = Date().time

        // We need to feed in float values between -1.0f and 1.0f, so divide the
        // signed 16-bit inputs.
        for (i in 0 until RECORDING_LENGTH) {
            floatInputBuffer[i][0] = audioData[i] / 32767.0f
        }

        val inputArray = arrayOf<Any>(floatInputBuffer, sampleRateList)
        val outputMap: MutableMap<Int, Any> = HashMap()
        var outputScores: Array<FloatArray>
        synchronized(labels) {
//                    Logger.debug("labels = $labels")
            outputScores = Array(1) { kotlin.FloatArray(labels.size) }
        }
        outputMap[0] = outputScores

        val inferenceStartTime = Date().time
        // Run the model.
        try {
            tfLite?.runForMultipleInputsOutputs(inputArray, outputMap)
        } catch (e: Exception) {
            Logger.error("inference failed: $e")
        }

        // Use the smoother to figure out if we've had a real recognition event.
        val currentTime = System.currentTimeMillis()
        val result: RecognitionResult? =
            recognizeCommands?.processLatestResults(outputScores[0], currentTime)
//                Logger.debug("result: $result")

        val endTime = Date().time

        lastProcessingTimeMs = endTime - startTime

        inferenceInfo.sampleRate = SAMPLE_RATE
        inferenceInfo.bufferSize = audioData.size
        inferenceInfo.inferenceTime = endTime - inferenceStartTime
        inferenceInfo.analysisTime = lastProcessingTimeMs
        inferenceAgent.deliverInferenceInfo(inferenceInfo)

        result?.let {
            inferenceAgent.deliverResults(result)
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(assets: AssetManager,
                              modelFilename: String): MappedByteBuffer? {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream =
            FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

}