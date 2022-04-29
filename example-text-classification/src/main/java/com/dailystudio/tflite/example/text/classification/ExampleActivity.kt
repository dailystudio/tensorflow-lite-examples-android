package com.dailystudio.tflite.example.text.classification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.devbricksx.utils.StringUtils
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.text.AbsChatActivity
import com.dailystudio.tflite.example.common.text.ChatRecord
import com.dailystudio.tflite.example.common.text.MessageType
import com.dailystudio.tflite.example.common.text.model.ChatRecordViewModel
import com.dailystudio.tflite.example.common.ui.ItemLabel
import com.dailystudio.tflite.example.common.ui.fragment.ItemLabelsListFragment
import com.dailystudio.tflite.example.common.ui.model.ItemLabelViewModel
import com.dailystudio.tflite.example.common.utils.ResultsUtils
import org.tensorflow.lite.examples.textclassification.TextClassificationClient
import org.tensorflow.lite.support.model.Model

class ExampleActivity : AbsChatActivity<Map<String, TextClassificationClient.Result>>() {

    companion object {
        const val FRAGMENT_TAG_RESULTS = "results-fragment"
        const val LABELS_FILE = "text_classification_labels.txt"

        const val LABEL_POSITIVE = "positive"
        const val LABEL_NEGATIVE = "negative"
    }

    private lateinit var client: TextClassificationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initItemLabels()

        lifecycleScope.launchWhenStarted {
            client = TextClassificationClient(
                applicationContext,
                Model.Device.CPU,
                4
            ).apply {
//                load()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        client.close()
    }

    override fun createResultsView(): View? {
        val stubView = LayoutInflater.from(this).inflate(
            R.layout.layout_results_view, null)

        supportFragmentManager.beginTransaction().also {
            val resultsFragment = ItemLabelsListFragment()

            it.add(R.id.results_fragment_stub, resultsFragment, FRAGMENT_TAG_RESULTS)
            it.show(resultsFragment)
            it.commitAllowingStateLoss()
        }

        return stubView
    }

    override fun onResultsUpdated(results: Map<String, TextClassificationClient.Result>) {
        val viewModel = ViewModelProvider(this).get(ItemLabelViewModel::class.java)

        val items = viewModel.getItemLabels()
        for (item in items) {
            val key = item.name.toLowerCase()
            if (results.containsKey(key)) {
                val result = results[key] ?: continue

                item.label = buildString {
                    append(item.name)
                    append(" (")
                    append("%3.1f%%".format(result.confidence * 100))
                    append(")")
                }

                viewModel.updateItemLabel(item)
            }
        }
    }

    override fun generateResults(text: String,
                                 info: InferenceInfo): Map<String, TextClassificationClient.Result>? {
        val results = client.classify(text)
        Logger.debug("results of [$text]: ${ResultsUtils.safeToPrintableLog(results)}")

        val map = mutableMapOf<String, TextClassificationClient.Result>()
        for (r in results) {
            map[r.title.toLowerCase()] = r
        }

        return map
    }

    override fun convertResultsToReplyText(results: Map<String, TextClassificationClient.Result>?,
                                           info: InferenceInfo): String {
        Logger.debug("convert results: ${ResultsUtils.safeToPrintableLog(results)}")
        val defaultPrompt = getString(R.string.prompt_default)
        if (results == null) {
            return defaultPrompt
        }

        val positive = results[LABEL_POSITIVE] ?: return defaultPrompt
        val negative = results[LABEL_NEGATIVE] ?: return defaultPrompt
        Logger.debug("positive: ${ResultsUtils.safeToPrintableLog(positive)}")
        Logger.debug("negative: ${ResultsUtils.safeToPrintableLog(negative)}")

        return getString(if (positive.confidence > negative.confidence) {
            R.string.prompt_positive
        } else {
            R.string.prompt_native
        })
    }

    override suspend fun insertLeadingRecords() {
        super.insertLeadingRecords()

        val viewModel = ViewModelProvider(this).get(ChatRecordViewModel::class.java)

        val records = viewModel.getChatRecords()
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
        val labels = StringUtils.linesFromAsset(this, LABELS_FILE)
        val viewModel = ViewModelProvider(this).get(ItemLabelViewModel::class.java)

        for ((i, l) in labels.withIndex()) {
            viewModel.insertItemLabel(ItemLabel(i, l, l))
        }
    }

    override fun getExampleName(): CharSequence? {
        return getString(R.string.app_name)
    }

    override fun getExampleIconResource(): Int {
        return R.drawable.about_icon
    }

    override fun getExampleDesc(): CharSequence? {
        return getString(R.string.app_desc)
    }

}