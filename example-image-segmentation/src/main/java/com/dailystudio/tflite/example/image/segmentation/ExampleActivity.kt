package com.dailystudio.tflite.example.image.segmentation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.development.Logger
import org.tensorflow.litex.ui.ItemLabel
import com.dailystudio.tflite.example.common.ui.fragment.ItemLabelsListFragment
import com.dailystudio.tflite.example.common.ui.model.ItemLabelViewModel
import com.dailystudio.tflite.example.image.segmentation.fragment.ImageSegmentationCameraFragment
import com.dailystudio.tflite.example.image.segmentation.ui.MaskOverlay
import org.tensorflow.lite.examples.imagesegmentation.SegmentationResult
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.activity.LiteUseCaseActivity

class ExampleActivity : LiteUseCaseActivity() {

    companion object {
        const val FRAGMENT_TAG_RESULTS = "results-fragment"
        const val MAX_ITEMS = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initItemLabels()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_image_segmentation
    }

    override fun createBaseFragment(): Fragment {
        return ImageSegmentationCameraFragment()
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
        when (nameOfUseCase) {
            SegmentationUseCase.UC_NAME -> {
                val mask = results as SegmentationResult

                val overlay: MaskOverlay? = findViewById(R.id.mask_overlay)
                overlay?.setMask(mask.maskBitmap)

                val viewModel = ViewModelProvider(this)[ItemLabelViewModel::class.java]

                val items = mask.items.toList()
                for (i in 0 until MAX_ITEMS) {
                    val item = viewModel.getItemLabel(i)
                    item?.let {
                        item.label = ""

                        if (i < items.size) {
                            it.label = items[i].first
                            it.color = items[i].second.toInt()

                            Logger.debug("APPLY S-COLOR [${it.label}]: ${it.color}")

                        }

                        viewModel.updateItemLabel(it)
                    }
                }
            }
        }
    }

    private fun initItemLabels() {
        val viewModel = ViewModelProvider(this).get(ItemLabelViewModel::class.java)

        for (i in 0 until MAX_ITEMS) {
            viewModel.insertItemLabel(ItemLabel(i, "", "", Color.WHITE))
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

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            SegmentationUseCase.UC_NAME to SegmentationUseCase()
        )
    }

}