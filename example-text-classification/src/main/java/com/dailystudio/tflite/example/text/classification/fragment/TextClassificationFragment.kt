package com.dailystudio.tflite.example.text.classification.fragment

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.StringUtils
import com.dailystudio.tensorflow.litex.ui.ItemLabel
import com.dailystudio.tensorflow.litex.utils.ResultsUtils
import com.dailystudio.tflite.example.text.classification.ExampleActivity
import com.dailystudio.tflite.example.text.classification.R
import com.dailystudio.tflite.example.text.classification.TextClassificationUseCase
import org.tensorflow.lite.examples.textclassification.TextClassificationClient
import com.dailystudio.tensorflow.litex.text.ChatRecord
import com.dailystudio.tensorflow.litex.text.LiteChatUseCaseFragment
import com.dailystudio.tensorflow.litex.text.MessageType
import com.dailystudio.tensorflow.litex.text.model.ChatRecordViewModel
import com.dailystudio.tensorflow.litex.ui.model.ItemLabelViewModel

class TextClassificationFragment: LiteChatUseCaseFragment() {

    companion object {
        const val LABELS_FILE = "text_classification_labels.txt"
    }

    override val nameOfUsedUseCase: String
        get() = TextClassificationUseCase.UC_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initItemLabels()
    }

    override fun convertResultsToReplyText(results: Any?): String {
        if (results is Map<*, *>) {
            Logger.debug("convert results: ${ResultsUtils.safeToPrintableLog(results)}")
            val defaultPrompt = getString(R.string.prompt_default)

            val positive = results[ExampleActivity.LABEL_POSITIVE]
                    as? TextClassificationClient.Result?: return defaultPrompt
            val negative = results[ExampleActivity.LABEL_NEGATIVE]
                    as? TextClassificationClient.Result ?: return defaultPrompt
            Logger.debug("positive: ${ResultsUtils.safeToPrintableLog(positive)}")
            Logger.debug("negative: ${ResultsUtils.safeToPrintableLog(negative)}")

            return getString(if (positive.confidence > negative.confidence) {
                R.string.prompt_positive
            } else {
                R.string.prompt_native
            })
        } else {
            return ""
        }
    }

    override suspend fun insertLeadingRecords() {
        super.insertLeadingRecords()

        val viewModel = ViewModelProvider(this)[ChatRecordViewModel::class.java]

        val records = viewModel.allChatRecords
        if (records.size > NOOP_RECORDS_COUNT) {
            return
        }

        val record = ChatRecord(System.currentTimeMillis(),
            getString(R.string.chat_text_question),
            MessageType.Receive
        )

        viewModel.insertChatRecord(record)
    }

    private fun initItemLabels() {
        val labels = StringUtils.linesFromAsset(requireContext(), LABELS_FILE)
        val viewModel = ViewModelProvider(this)[ItemLabelViewModel::class.java]

        for ((i, l) in labels.withIndex()) {
            viewModel.insertItemLabel(ItemLabel(i, l, l))
        }
    }

}