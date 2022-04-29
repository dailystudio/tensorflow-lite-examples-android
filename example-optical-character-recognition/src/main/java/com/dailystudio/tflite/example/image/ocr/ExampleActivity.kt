package com.dailystudio.tflite.example.image.ocr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.ItemLabel
import com.dailystudio.tflite.example.common.ui.fragment.ItemLabelsListFragment
import com.dailystudio.tflite.example.common.ui.model.ItemLabelViewModel
import com.dailystudio.tflite.example.image.ocr.fragment.OpticalCharacterRecognitionCameraFragment
import org.tensorflow.lite.examples.ocr.ModelExecutionResult

class ExampleActivity : AbsExampleActivity<OCRInferenceInfo, ModelExecutionResult>() {

    companion object {
        const val FRAGMENT_TAG_RESULTS = "results-fragment"
        const val MAX_ITEMS = 3
    }

    private var resultOverlay: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initItemLabels()
    }

    override fun setupViews() {
        super.setupViews()
        resultOverlay = findViewById(R.id.result_overlay)
    }

    private fun initItemLabels() {
        val viewModel = ViewModelProvider(this).get(ItemLabelViewModel::class.java)

        for (i in 0 until MAX_ITEMS) {
            viewModel.insertItemLabel(ItemLabel(i, "", ""))
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_optical_character_recognition
    }

    override fun createBaseFragment(): Fragment {
        return OpticalCharacterRecognitionCameraFragment()
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

    override fun onResultsUpdated(results: ModelExecutionResult) {
        Logger.debug("items: ${results.itemsFound}")

        resultOverlay?.setImageBitmap(results.bitmapResult)

        val viewModel = ViewModelProvider(this).get(ItemLabelViewModel::class.java)

        val items = results.itemsFound.toList()

        if (items.isNotEmpty()) {
            for (i in 0 until MAX_ITEMS) {
                val item = viewModel.getItemLabel(i)
                item?.let {
                    item.label = ""

                    if (i < items.size) {
                        it.label = items[i].first
                    }

                    viewModel.updateItemLabel(it)
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

}
