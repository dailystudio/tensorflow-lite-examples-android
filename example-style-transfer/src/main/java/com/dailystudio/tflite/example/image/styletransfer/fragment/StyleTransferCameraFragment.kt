package com.dailystudio.tflite.example.image.styletransfer.fragment

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dailystudio.tflite.example.image.styletransfer.StyleTransferPrefs
import com.dailystudio.tflite.example.image.styletransfer.StyleTransferUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.fragment.LiteCameraUseCaseFragment
import com.dailystudio.tensorflow.litex.getLiteUseCaseViewModel

class StyleTransferCameraFragment : LiteCameraUseCaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                StyleTransferPrefs.prefsChanges.collect {
                    if (it.prefKey == StyleTransferPrefs.KEY_SELECTED_STYLE) {
                        val useCase = liteUseCaseViewModel.getUseCase(StyleTransferUseCase.UC_NAME)

                        if (useCase is StyleTransferUseCase) {
                            useCase.selectStyle(StyleTransferPrefs.getSelectedStyle(requireContext()))
                        }
                    }
                }
            }
        }
    }

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(
            StyleTransferUseCase.UC_NAME
        )

}