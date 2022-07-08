package com.dailystudio.tflite.example.image.classification

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.image.classification.fragment.ImageClassificationCameraFragment
import com.dailystudio.tflite.example.image.classification.fragment.ImageClassificationSettingsFragment
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity
import com.dailystudio.tensorflow.litex.image.Recognition
import kotlin.math.min

class ExampleActivity : LiteUseCaseActivity() {

    companion object {
        const val REPRESENTED_ITEMS_COUNT = 3
    }

    private var detectItemViews: Array<TextView?> =
        Array(REPRESENTED_ITEMS_COUNT) {null}
    private var detectItemValueViews: Array<TextView?> =
        Array(REPRESENTED_ITEMS_COUNT) {null}

    override fun createBaseFragment(): Fragment {
        return ImageClassificationCameraFragment()
    }

    override fun createResultsView(): View? {
        val resultsView = LayoutInflater.from(this).inflate(
            R.layout.layout_results, null)

        resultsView?.let {
            setupResultView(it)
        }

        return resultsView
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        when (nameOfUseCase) {
            ClassifierUseCase.UC_NAME -> {
                val list = results as List<*>

                val itemCount = min(list.size, REPRESENTED_ITEMS_COUNT)

                for (i in 0 until itemCount) {
                    val recognition = list[i] as Recognition
                    detectItemViews[i]?.text = recognition.title
                    detectItemValueViews[i]?.text = "%.1f%%".format((recognition.confidence ?: 0f) * 100)
                }
            }
        }

    }

    private fun setupResultView(resultsView: View) {
        for (i in 0 until REPRESENTED_ITEMS_COUNT) {
            detectItemViews[i] = resultsView.findViewById(
                resources.getIdentifier("detected_item${i + 1}", "id", packageName)
            )

            detectItemValueViews[i] = resultsView.findViewById(
                resources.getIdentifier("detected_item${i + 1}_value", "id", packageName)
            )
        }
    }

    override fun getExampleIconResource(): Int {
        return R.drawable.about_icon
    }

    override fun getExampleName(): CharSequence {
        return getString(R.string.app_name)
    }

    override fun getExampleDesc(): CharSequence? {
        return getString(R.string.app_desc)
    }

    override fun getExampleThumbVideoResource(): Int {
        return R.raw.image_classification_720p
    }

    override fun createSettingsFragment(): AbsSettingsDialogFragment? {
        return ImageClassificationSettingsFragment()
    }

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            ClassifierUseCase.UC_NAME to ClassifierUseCase()
        )
    }

}
