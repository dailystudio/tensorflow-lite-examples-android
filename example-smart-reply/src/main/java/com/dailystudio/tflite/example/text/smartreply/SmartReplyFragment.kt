package com.dailystudio.tflite.example.text.smartreply

import org.tensorflow.lite.examples.smartreply.SmartReply
import com.dailystudio.tensorflow.litex.text.LiteChatUseCaseFragment

class SmartReplyFragment: LiteChatUseCaseFragment() {
    override val nameOfUsedUseCase: String
        get() = SmartReplyUseCase.UC_NAME

    override fun convertResultsToReplyText(results: Any?): String {
        return if (results is Array<*>) {
            if (results.isEmpty()) {
                ""
            } else {
                (results[0] as SmartReply).text
            }
        } else {
            ""
        }
    }

}