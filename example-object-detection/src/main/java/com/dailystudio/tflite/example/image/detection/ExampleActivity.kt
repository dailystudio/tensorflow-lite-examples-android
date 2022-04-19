package com.dailystudio.tflite.example.image.detection

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.settings.AbsSetting
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsFragment
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.detection.fragment.ObjectDetectionCameraFragment
import org.tensorflow.lite.examples.detection.customview.OverlayView
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker
import org.tensorflow.litex.images.Recognition

class ObjectDetectionSettingsFragment: InferenceSettingsFragment() {
    override fun createSettings(context: Context): Array<AbsSetting> {
        val settings = super.createSettings(context)

        return settings.filter {
            it.name != InferenceSettingsPrefs.PREF_ENABLE_IMAGE_PREPROCESS
        }.toTypedArray()
    }
}

class ExampleActivity : AbsExampleActivity<ImageInferenceInfo, List<Recognition>>() {

    private lateinit var tracker: MultiBoxTracker
    private var trackingOverlay: OverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tracker = MultiBoxTracker(this)
        trackingOverlay =
            findViewById(R.id.tracking_overlay)
        trackingOverlay?.addCallback{ canvas ->
            tracker.draw(canvas)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_object_detection
    }

    override fun createBaseFragment(): Fragment {
        return ObjectDetectionCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: List<Recognition>) {
        tracker.trackResults(results, System.currentTimeMillis())
        trackingOverlay?.postInvalidate()
    }

    override fun onInferenceInfoUpdated(info: ImageInferenceInfo) {
        super.onInferenceInfoUpdated(info)

        tracker.setFrameConfiguration(
            info.imageSize.width, info.imageSize.height,
            info.imageRotation)
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

    override fun createSettingsFragment(): AbsSettingsDialogFragment? {
        return ObjectDetectionSettingsFragment();
    }
}
