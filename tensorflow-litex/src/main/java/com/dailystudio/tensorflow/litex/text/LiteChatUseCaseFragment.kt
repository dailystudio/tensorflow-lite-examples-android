package com.dailystudio.tensorflow.litex.text

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.tensorflow.litex.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.dailystudio.tensorflow.litex.LiteUseCaseViewModel
import com.dailystudio.tensorflow.litex.getLiteUseCaseViewModel
import com.dailystudio.tensorflow.litex.text.model.ChatRecordViewModel

abstract class LiteChatUseCaseFragment: ChatRecordListFragmentExt() {

    companion object {
        const val NOOP_RECORDS_COUNT = 1
        const val RANDOM_RECORDS_COUNT = 20
    }

    private var userInput: EditText? = null
    private var sendButton: Button? = null

    protected val liteUseCaseViewModel: LiteUseCaseViewModel
        get() {
            return getLiteUseCaseViewModel()
        }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(getLayoutResId(), container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userInput = view.findViewById(R.id.input)
        userInput?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton?.isEnabled = !(s == null || s.isEmpty())
            }
        })

        sendButton = view.findViewById(R.id.send_button)
        sendButton?.setOnClickListener{
            val editable = userInput?.text ?: return@setOnClickListener
            val text = editable.toString()

            lifecycleScope.launch(Dispatchers.IO) {
                sendMessage(text)
                receiveReply(text)
            }

            editable.clear()
        }

        lifecycleScope.launchWhenStarted {
            insertLeadingRecords()
//
//            val info = InferenceInfo()
//            inferenceAgent.deliverInferenceInfo(info)
        }
    }

    open fun getLayoutResId(): Int {
        return R.layout.fragment_chat
    }

    protected open suspend fun insertLeadingRecords() {
        val viewModel = ViewModelProvider(this)[ChatRecordViewModel::class.java]

        val records = viewModel.allChatRecords
        if (records.isNotEmpty()) {
            return
        }

        for (i in 0 until NOOP_RECORDS_COUNT) {
            val record = ChatRecord(System.currentTimeMillis(),
                "",
                MessageType.Noop
            )

            viewModel.insertChatRecord(record)
        }
    }

    private suspend fun randomlyGenerateRecords() {
        val viewModel = ViewModelProvider(this)[ChatRecordViewModel::class.java]

        var direction = MessageType.Send
        var humanId = 0
        var robotId = 0
        for (i in 0 until RANDOM_RECORDS_COUNT) {
            val record = ChatRecord(System.currentTimeMillis(),
                if (direction == MessageType.Send) {
                    "Hi there, nice to meet you!"
                } else {
                    "Hi, I am Robot-No.[${robotId++}]. Nice to meet you too."
                },
                direction
            )

            direction = if (direction == MessageType.Send) {
                MessageType.Receive
            } else {
                MessageType.Send
            }

            viewModel.insertChatRecord(record)

            delay(1000)
        }
    }

    private fun sendMessage(text: String) {
        val viewModel = ViewModelProvider(this)[ChatRecordViewModel::class.java]

        val record = ChatRecord(System.currentTimeMillis(),
            text,
            MessageType.Send)

        viewModel.insertChatRecord(record)
    }

    private fun receiveReply(text: String) {
        val result = liteUseCaseViewModel.performUseCase(nameOfUsedUseCase, text)
        val replyText = convertResultsToReplyText(result)

        val viewModel = ViewModelProvider(this)[ChatRecordViewModel::class.java]

        val record = ChatRecord(System.currentTimeMillis(),
            replyText,
            MessageType.Receive)

        viewModel.insertChatRecord(record)
    }

    protected abstract val nameOfUsedUseCase: String

    abstract fun convertResultsToReplyText(results: Any?): String

}
