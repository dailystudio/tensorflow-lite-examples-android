package com.dailystudio.tflite.example.speech.recognition.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.AbsPermissionsFragment
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.speech.recognition.R
import com.dailystudio.tflite.example.speech.recognition.async.ManagedThread
import kotlinx.android.synthetic.main.fragment_speech_recognition.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.examples.speech.RecognizeCommands
import org.tensorflow.lite.examples.speech.RecognizeCommands.RecognitionResult
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max
import kotlin.math.roundToInt

class SpeechRecognitionFragment : AbsPermissionsFragment() {

    companion object {
        val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS)

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

    private var recordingBuffer = ShortArray(RECORDING_LENGTH)
    private var recordingOffset = 0
    private val recordingBufferLock = ReentrantLock()

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

    override fun getPermissionsPromptViewId(): Int {
        return R.id.permission_prompt
    }

    override fun getRequiredPermissions(): Array<String> {
        return PERMISSIONS_REQUIRED
    }

    override fun onPermissionsDenied() {
    }

    override fun onPermissionsGranted(newlyGranted: Boolean) {
        lifecycleScope.launchWhenResumed {
            startRecording()
            startRecognizing()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.cancel()
        startRecording()
        startRecognizing()
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.cancel()

        stopRecording()
        stopRecognizing()
    }

    private fun startRecording() {
        recordingThread.start()
    }

    private fun stopRecording() {
        recordingThread.stop()
    }

    private fun startRecognizing() {
        recognitionThread.start()
    }

    private fun stopRecognizing() {
        recognitionThread.stop()
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

    private var recordingThread: ManagedThread = object : ManagedThread() {

        override fun runInBackground() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            // Estimate the buffer size we'll need for this device.
            var bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                bufferSize = SAMPLE_RATE * 2
            }

            val audioBuffer = ShortArray(bufferSize / 2)

            val record = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Logger.error("audio record initialization failed: state = ${record.state}")
                return
            }

            record.startRecording()

            // Loop, gathering audio data and copying it to a round-robin buffer.
            while (isRunning) {
                val numberRead = record.read(audioBuffer, 0, audioBuffer.size) ?: 0

                val maxLength: Int = recordingBuffer.size
                val newRecordingOffset: Int = recordingOffset + numberRead
                val secondCopyLength = max(0, newRecordingOffset - maxLength)
                val firstCopyLength = numberRead - secondCopyLength

                // We store off all the data for the recognition thread to access. The ML
                // thread will copy out of this buffer into its own, while holding the
                // lock, so this should be thread safe.
                recordingBufferLock.lock()
                try {
                    System.arraycopy(audioBuffer, 0,
                        recordingBuffer, recordingOffset, firstCopyLength)
                    System.arraycopy(audioBuffer, firstCopyLength,
                        recordingBuffer, 0, secondCopyLength)

                    recordingOffset = newRecordingOffset % maxLength
                } finally {
                    recordingBufferLock.unlock()
                }
            }

            Logger.info("stop recording: record = $record")

            record?.stop()
            record?.release()
        }

    }

    private var recognitionThread: ManagedThread = object : ManagedThread() {

        override fun runInBackground() {
            val inferenceInfo = InferenceInfo()

            val inputBuffer = ShortArray(RECORDING_LENGTH)
            val floatInputBuffer = Array(RECORDING_LENGTH) { FloatArray(1) }
            val sampleRateList = intArrayOf(SAMPLE_RATE)

            // Loop, grabbing recorded data and running the recognition model on it.

            // Loop, grabbing recorded data and running the recognition model on it.
            while (isRunning) {
                val startTime = Date().time
                // The recording thread places data in this round-robin buffer, so lock to
                // make sure there's no writing happening and then copy it to our own
                // local version.
                recordingBufferLock.lock()
                try {
                    val maxLength = recordingBuffer.size
                    val firstCopyLength = maxLength - recordingOffset
                    val secondCopyLength = recordingOffset
                    System.arraycopy(recordingBuffer, recordingOffset,
                        inputBuffer, 0, firstCopyLength)
                    System.arraycopy(recordingBuffer, 0,
                        inputBuffer, firstCopyLength, secondCopyLength)
                } finally {
                    recordingBufferLock.unlock()
                }

                // We need to feed in float values between -1.0f and 1.0f, so divide the
                // signed 16-bit inputs.
                for (i in 0 until RECORDING_LENGTH) {
                    floatInputBuffer[i][0] = inputBuffer[i] / 32767.0f
                }

                val inputArray = arrayOf<Any>(floatInputBuffer, sampleRateList)
                val outputMap: MutableMap<Int, Any> = HashMap()
                var outputScores: Array<FloatArray>
                synchronized(labels) {
//                    Logger.debug("labels = $labels")
                    outputScores = Array(1) { kotlin.FloatArray(labels.size) }
                }
                outputMap[0] = outputScores

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
                lastProcessingTimeMs = Date().time - startTime

                inferenceInfo.inferenceTime = lastProcessingTimeMs
                inferenceInfo.analysisTime = lastProcessingTimeMs
                inferenceAgent.deliverInferenceInfo(inferenceInfo)

                result?.let {
                    inferenceAgent.deliverResults(result)
                }

                try {
                    // We don't need to run too frequently, so snooze for a bit.
                    Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS)
                } catch (e: InterruptedException) {
                    // Ignore
                }
            }
        }
    }
}