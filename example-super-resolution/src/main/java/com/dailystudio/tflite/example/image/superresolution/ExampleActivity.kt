package com.dailystudio.tflite.example.image.superresolution

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.image.superresolution.fragment.SuperResolutionCameraFragment
import com.dailystudio.tflite.example.image.superresolution.model.SuperRes
import com.dailystudio.tflite.example.image.superresolution.ui.ImageClipOverlay
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity

class ExampleActivity : LiteUseCaseActivity() {

    private var clipOverlay: ImageClipOverlay? = null
    private var superResOverlay: ImageClipOverlay? = null

    override fun setupViews() {
        super.setupViews()

        clipOverlay = findViewById(R.id.clip_overlay)
        superResOverlay = findViewById(R.id.super_res_overlay)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_super_resolution
    }

    override fun createBaseFragment(): Fragment {
        return SuperResolutionCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
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
        return R.raw.super_resolution_720p
    }

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            SuperResUseCase.UC_NAME to SuperResUseCase()
        )
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        when(nameOfUseCase) {
            SuperResUseCase.UC_NAME -> {
                if (results is SuperRes) {
                    Logger.debug("[RES]: $results")

                    clipOverlay?.setClipBitmap(results.originalBitmap)
                    superResOverlay?.setClipBitmap(results.superBitmap)
                }
            }
        }
    }

}
