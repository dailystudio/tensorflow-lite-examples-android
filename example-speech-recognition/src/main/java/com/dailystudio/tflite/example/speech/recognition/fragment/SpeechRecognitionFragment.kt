package com.dailystudio.tflite.example.speech.recognition.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dailystudio.devbricksx.audio.AudioConfig
import com.dailystudio.devbricksx.audio.AudioProcessFragment
import com.dailystudio.tflite.example.speech.recognition.R
import com.dailystudio.tflite.example.speech.recognition.SpeechRecognitionUseCase
import org.tensorflow.litex.getLiteUseCaseViewModel

class SpeechRecognitionFragment : AudioProcessFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_speech_recognition, container, false)

    override fun onProcessAudioData(audioConfig: AudioConfig, audioData: ShortArray) {
        val viewModel = getLiteUseCaseViewModel()

        viewModel.performUseCase(SpeechRecognitionUseCase.UC_NAME, audioData)
    }

}