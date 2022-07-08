package com.dailystudio.tflite.example.text.classification

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.tflite.example.text.classification.fragment.TextClassificationFragment
import org.tensorflow.lite.examples.textclassification.TextClassificationClient
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity
import com.dailystudio.tensorflow.litex.ui.fragment.ItemLabelsListFragment
import com.dailystudio.tensorflow.litex.ui.model.ItemLabelViewModel
import java.util.*

class ExampleActivity : LiteUseCaseActivity() {
    companion object {
        const val FRAGMENT_TAG_RESULTS = "results-fragment"

        const val LABEL_POSITIVE = "positive"
        const val LABEL_NEGATIVE = "negative"
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

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        when(nameOfUseCase) {
            TextClassificationUseCase.UC_NAME -> {
                val viewModel = ViewModelProvider(this)[ItemLabelViewModel::class.java]

                val items = viewModel.getItemLabels()
                for (item in items) {
                    val key = item.name.lowercase(Locale.getDefault())
                    if (results is Map<*, *>) {
                        if (results.containsKey(key)) {
                            val result = results[key] as? TextClassificationClient.Result ?: continue

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
            }
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

    override fun getExampleThumbVideoResource(): Int {
        return R.raw.text_classification_720p
    }

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            TextClassificationUseCase.UC_NAME to TextClassificationUseCase()
        )
    }

    override fun createBaseFragment(): Fragment {
        return TextClassificationFragment()
    }

}