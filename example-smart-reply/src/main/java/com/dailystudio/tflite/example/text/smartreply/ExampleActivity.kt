package com.dailystudio.tflite.example.text.smartreply

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.text.smartreply.fragment.ChatRecordListFragmentExt
import com.dailystudio.tflite.example.text.smartreply.model.ChatRecordViewModel
import kotlinx.coroutines.delay

class ExampleActivity : AbsExampleActivity<InferenceInfo, Void>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenResumed {
            randomlyGenerateRecords()
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_smart_reply
    }

    private suspend fun randomlyGenerateRecords() {
        val viewModel = ViewModelProvider(this).get(ChatRecordViewModel::class.java)

        var direction = Direction.Send
        for (i in 0 until 20) {
            val record = ChatRecord(System.currentTimeMillis(),
                if (direction == Direction.Send) {
                    "Hi there, I am [$i]"
                } else {
                    "Hi [${i - 1}], How are you?"
                },
                direction
            )

            direction = if (direction == Direction.Send) {
                Direction.Receive
            } else {
                Direction.Send
            }

            viewModel.insertChatRecord(record)

            delay(1000)
        }
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

    override fun onResultsUpdated(results: Void) {
    }

}
