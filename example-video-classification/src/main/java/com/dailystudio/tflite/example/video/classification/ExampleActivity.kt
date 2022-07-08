package com.dailystudio.tflite.example.video.classification

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.video.classification.fragment.VideoClassificationCameraFragment
import com.dailystudio.tflite.example.video.classification.fragment.VideoClassificationSettingsFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.tensorflow.lite.support.label.Category
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity
import com.dailystudio.tensorflow.litex.getLiteUseCaseViewModel
import kotlin.math.min

class ExampleActivity : LiteUseCaseActivity() {

    companion object {
        const val REPRESENTED_ITEMS_COUNT = 3
    }

    private var detectItemViews: Array<TextView?> =
        Array(REPRESENTED_ITEMS_COUNT) {null}
    private var detectItemValueViews: Array<TextView?> =
        Array(REPRESENTED_ITEMS_COUNT) {null}

    private var fabResetState: FloatingActionButton? = null

    override fun setupViews() {
        super.setupViews()


        fabResetState = findViewById(R.id.fab_reset_state)
        fabResetState?.setOnClickListener {
            val useCase = getLiteUseCaseViewModel().getUseCase(VideoClassificationUseCase.UC_NAME)
            if (useCase is VideoClassificationUseCase) {
                useCase.resetVideoState()
            }
        }

    }
    private fun setupResultView(resultsView: View) {
        for (i in 0 until REPRESENTED_ITEMS_COUNT) {
            detectItemViews[i] = resultsView.findViewById<TextView?>(
                resources.getIdentifier("detected_item${i + 1}", "id", packageName)
            )
            detectItemValueViews[i] = resultsView.findViewById(
                resources.getIdentifier("detected_item${i + 1}_value", "id", packageName)
            )
        }
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        when (nameOfUseCase) {
            VideoClassificationUseCase.UC_NAME -> {
                if (results is List<*>) {
                    val itemCount = min(results.size, REPRESENTED_ITEMS_COUNT)

                    for (i in 0 until itemCount) {
                        val item = results[i] as Category
                        detectItemViews[i]?.text = item.label
                        detectItemValueViews[i]?.text = "%.1f%%".format((item.score ?: 0f) * 100)
                    }
                }
            }
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
        return R.raw.video_classification_720p
    }

    override fun createSettingsFragment(): AbsSettingsDialogFragment? {
        return VideoClassificationSettingsFragment()
    }

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            VideoClassificationUseCase.UC_NAME to VideoClassificationUseCase()
        )
    }

    override fun createBaseFragment(): Fragment {
        return VideoClassificationCameraFragment()
    }

    override fun createResultsView(): View? {
        val resultsView = LayoutInflater.from(this).inflate(
            R.layout.layout_results, null)

        resultsView?.let {
            setupResultView(it)
        }

        return resultsView
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_video_classification
    }
}
