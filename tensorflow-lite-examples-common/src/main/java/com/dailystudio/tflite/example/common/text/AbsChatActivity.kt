package com.dailystudio.tflite.example.common.text

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.R
import com.dailystudio.tflite.example.common.text.model.ChatRecordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class AbsChatActivity<Results> : AbsExampleActivity<InferenceInfo, Results>() {

    companion object {
        const val NOOP_RECORDS_COUNT = 1
        const val RANDOM_RECORDS_COUNT = 20
    }

    private var userInput: EditText? = null
    private var sendButton: Button? = null

    private var inferenceAgent: InferenceAgent<InferenceInfo, Results> =
        InferenceAgent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userInput = findViewById(R.id.input)
        userInput?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton?.isEnabled = !(s == null || s.isEmpty())
            }
        })

        sendButton = findViewById(R.id.send_button)
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

            val info = InferenceInfo()
            inferenceAgent.deliverInferenceInfo(info)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_chat
    }

    override fun createBaseFragment(): Fragment {
        return ChatRecordListFragmentExt()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createSettingsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: Results) {
    }

    protected open suspend fun insertLeadingRecords() {
        val viewModel = ViewModelProvider(this).get(ChatRecordViewModel::class.java)

        val records = viewModel.getChatRecords()
        if (records.isNotEmpty()) {
            return
        }

        for (i in 0 until NOOP_RECORDS_COUNT) {
            val record = ChatRecord(System.currentTimeMillis(),
                "",
                MessageType.Noop
            )

            viewModel.insertChatRecord(record).join()
        }
    }

    private suspend fun randomlyGenerateRecords() {
        val viewModel = ViewModelProvider(this).get(ChatRecordViewModel::class.java)

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

            viewModel.insertChatRecord(record).join()

            delay(1000)
        }
    }

    private suspend fun sendMessage(text: String) {
        val viewModel = ViewModelProvider(this).get(ChatRecordViewModel::class.java)

        val record = ChatRecord(System.currentTimeMillis(),
            text,
            MessageType.Send)

        viewModel.insertChatRecord(record).join()
    }

    private suspend fun receiveReply(text: String) {
        val info = InferenceInfo()

        val start = System.currentTimeMillis()
        val results = generateResults(text, info)
        val inferenceEnd = System.currentTimeMillis()
        val replyText = convertResultsToReplyText(results, info)
        val end = System.currentTimeMillis()

        info.analysisTime = end - start
        info.inferenceTime = inferenceEnd - start

        inferenceAgent.deliverInferenceInfo(info)

        results?.let {
            inferenceAgent.deliverResults(it)
        }

        val viewModel = ViewModelProvider(this).get(ChatRecordViewModel::class.java)

        val record = ChatRecord(System.currentTimeMillis(),
            replyText,
            MessageType.Receive)

        viewModel.insertChatRecord(record).join()
    }

    abstract fun generateResults(text: String,
                                 info: InferenceInfo): Results?

    abstract fun convertResultsToReplyText(results: Results?,
                                           info: InferenceInfo): String

}
