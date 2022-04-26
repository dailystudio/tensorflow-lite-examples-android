package com.dailystudio.tflite.example.image.superresolution

import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.image.superresolution.fragment.SuperResolutionCameraFragment
import com.dailystudio.tflite.example.image.superresolution.model.SuperRes
import com.dailystudio.tflite.example.image.superresolution.ui.SuperResOverlay

class ExampleActivity : AbsExampleActivity<InferenceInfo, SuperRes>() {

    private var superResOverlay: SuperResOverlay? = null

    override fun setupViews() {
        super.setupViews()

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

    override fun onResultsUpdated(results: SuperRes) {
        superResOverlay?.setSuperResBitmap(results.originalBitmap)
    }

}
