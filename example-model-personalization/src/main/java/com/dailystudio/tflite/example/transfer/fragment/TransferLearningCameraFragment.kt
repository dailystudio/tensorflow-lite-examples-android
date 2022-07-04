package com.dailystudio.tflite.example.transfer.fragment

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.tflite.example.transfer.TransferLearningUseCase
import org.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class TransferLearningCameraFragment : LiteCameraUseCaseFragment() {

    private lateinit var viewModel: TransferLearningViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransferLearningViewModel::class.java]
    }

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(
            TransferLearningUseCase.UC_NAME
        )

}