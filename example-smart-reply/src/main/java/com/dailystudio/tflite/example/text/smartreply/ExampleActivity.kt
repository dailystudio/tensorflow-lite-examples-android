package com.dailystudio.tflite.example.text.smartreply

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.text.AbsChatActivity
import org.tensorflow.lite.examples.smartreply.SmartReplyClient

class ExampleActivity : AbsChatActivity() {

    private lateinit var client: SmartReplyClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client = SmartReplyClient(applicationContext)

        lifecycleScope.launchWhenStarted {
            client.loadModel()
        }
    }

    override fun generateReply(text: String,
                               info: InferenceInfo): String {
        val start = System.currentTimeMillis()
        val ans = client.predict(arrayOf(text))
        val end = System.currentTimeMillis()

        info.inferenceTime = end - start
        info.analysisTime = info.inferenceTime

        for (reply in ans) {
            Logger.debug("Reply: ${reply.text}")
        }

        if (ans == null || ans.isEmpty()) {
            return ""
        }

        return ans[0].text
    }

}
