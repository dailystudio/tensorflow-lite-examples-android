package com.dailystudio.tflite.example.speech.recognition.fragment

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.AbsPermissionsFragment
import com.dailystudio.tflite.example.speech.recognition.R
import com.dailystudio.tflite.example.speech.recognition.async.ManagedThread
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max

class SpeechRecognitionFragment : AbsPermissionsFragment() {

    companion object {
        val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.RECORD_AUDIO)

        // you are running your own model.
        private const val SAMPLE_RATE = 16000
        private const val SAMPLE_DURATION_MS = 1000
        private const val RECORDING_LENGTH = (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000)
    }

    private var recordingBuffer = ShortArray(RECORDING_LENGTH)
    private var recordingOffset = 0
    private val recordingBufferLock = ReentrantLock()

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
        startRecording()
    }

    override fun onStop() {
        super.onStop()

        stopRecording()
    }

    private fun startRecording() {
        recordingThread.start()
    }

    private fun stopRecording() {
        recordingThread.stop()
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
                bufferSize)

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Logger.error("audio record initialization failed: state = ${record.state}")
                return
            }

            record.startRecording()
            Logger.info("start recording: record = $record")

            // Loop, gathering audio data and copying it to a round-robin buffer.
            while (isRunning) {
                val numberRead = record.read(audioBuffer, 0, audioBuffer.size)
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

            record.stop()
            record.release()
        }

    }

}