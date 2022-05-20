package org.tensorflow.lite.examples.speech

import android.content.Context
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.speech.recognition.fragment.SpeechRecognitionFragment
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.TFLiteModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class CommandRecognizer(context: Context,
                        device: Model.Device,
                        numOfThreads: Int,
                        useXNNPack: Boolean
): TFLiteModel(context, MODEL_FILENAME, device, numOfThreads, useXNNPack) {

    companion object {
        private const val LABEL_FILENAME = "conv_actions_labels.txt"
        private const val MODEL_FILENAME = "conv_actions_frozen.tflite"

        const val SAMPLE_RATE = 16000
        private const val SAMPLE_DURATION_MS = 1000
        const val RECORDING_LENGTH = (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000)
        private const val AVERAGE_WINDOW_DURATION_MS: Long = 1000
        private const val DETECTION_THRESHOLD = 0.50f
        private const val SUPPRESSION_MS = 1500
        private const val MINIMUM_COUNT = 3
        private const val MINIMUM_TIME_BETWEEN_SAMPLES_MS: Long = 30
    }

    private val labels: MutableList<String> = mutableListOf()
    private val displayedLabels: MutableList<String> = mutableListOf()
    private var recognizeCommands: RecognizeCommands? = null

    init {
        initLabels()
    }

    private fun initLabels() {
        // Load the labels for the model, but only display those that don't start
        // with an underscore.
        val actualLabelFilename: String = LABEL_FILENAME
        Logger.debug("reading labels from: $actualLabelFilename")
        var br: BufferedReader? = null
        try {
            val stream = context.assets.open(actualLabelFilename)
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


        getInterpreter()?.resizeInput(0, intArrayOf(RECORDING_LENGTH, 1))
        getInterpreter()?.resizeInput(1, intArrayOf(1))
    }

    fun recognizeCommand(data: Array<FloatArray>): RecognizeCommands.RecognitionResult? {
        val sampleRateList = intArrayOf(SAMPLE_RATE)

        val startTime = Date().time

        val inputArray = arrayOf<Any>(data, sampleRateList)
        val outputMap: MutableMap<Int, Any> = HashMap()
        var outputScores: Array<FloatArray>
        synchronized(labels) {
//                    Logger.debug("labels = $labels")
            outputScores = Array(1) { FloatArray(labels.size) }
        }

        outputMap[0] = outputScores
        // Run the model.
        try {
            getInterpreter()?.runForMultipleInputsOutputs(inputArray, outputMap)
        } catch (e: Exception) {
            Logger.error("inference failed: $e")
        }

        // Use the smoother to figure out if we've had a real recognition event.
        val currentTime = System.currentTimeMillis()
        val result: RecognizeCommands.RecognitionResult? =
            recognizeCommands?.processLatestResults(outputScores[0], currentTime)
                Logger.debug("result: $result")

        return result
    }
}