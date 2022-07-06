package com.dailystudio.tflite.example.image.detection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.tensorflow.litex.InferenceInfo
import org.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.image.detection.fragment.ObjectDetectionCameraFragment
import org.tensorflow.lite.examples.detection.customview.OverlayView
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.activity.LiteUseCaseActivity
import org.tensorflow.litex.image.Recognition

class ExampleActivity : LiteUseCaseActivity() {

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

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        when (nameOfUseCase) {
            DetectorUseCase.UC_NAME -> {
                val recognitions = results as List<Recognition>

                tracker.trackResults(recognitions, System.currentTimeMillis())
                trackingOverlay?.postInvalidate()
            }
        }
    }

    override fun onInferenceInfoUpdated(nameOfUseCase: String, info: InferenceInfo) {
        super.onInferenceInfoUpdated(nameOfUseCase, info)

        when (nameOfUseCase) {
            DetectorUseCase.UC_NAME -> {
                val imageInferenceInfo = info as ImageInferenceInfo
                tracker.setFrameConfiguration(
                    imageInferenceInfo.imageSize.width, imageInferenceInfo.imageSize.height,
                    imageInferenceInfo.imageRotation)
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

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            DetectorUseCase.UC_NAME to DetectorUseCase()
        )
    }

}
