package com.dailystudio.tflite.example.text.smartreply

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.text.AbsChatActivity
import org.tensorflow.lite.examples.smartreply.SmartReply
import org.tensorflow.lite.examples.smartreply.SmartReplyClient

class ExampleActivity : AbsChatActivity<Array<SmartReply>>() {

    private lateinit var client: SmartReplyClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client = SmartReplyClient(applicationContext)

        lifecycleScope.launchWhenStarted {
            client.loadModel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        client.unloadModel()
    }

    override fun generateResults(text: String, info: InferenceInfo): Array<SmartReply>? {
        val ans = client.predict(arrayOf(text))
        for (reply in ans) {
            Logger.debug("Reply: ${reply.text}")
        }

        return ans
    }

    override fun convertResultsToReplyText(results: Array<SmartReply>?,
                                           info: InferenceInfo): String {
        if (results == null || results.isEmpty()) {
            return ""
        }

        return results[0].text
    }

    override fun createSettingsFragment(): AbsSettingsDialogFragment? {
        TODO("Not yet implemented")
    }

}
