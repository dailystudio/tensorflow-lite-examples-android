package com.dailystudio.tflite.example.image.detection

import android.os.Bundle
import android.view.View
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.AbsExampleFragment
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceInfoView
import com.dailystudio.tflite.example.image.detection.fragment.ObjectDetectionFragment
import org.tensorflow.lite.examples.detection.customview.OverlayView
import org.tensorflow.lite.examples.detection.tflite.Classifier
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker

class ExampleActivity : AbsExampleActivity<InferenceInfo, List<Classifier.Recognition>>() {

    private var inferenceInfoView: InferenceInfoView? = null

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

    override fun createExampleFragment(): AbsExampleFragment<InferenceInfo, List<Classifier.Recognition>> {
        return ObjectDetectionFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun createInferenceInfoView(): View? {
        inferenceInfoView = InferenceInfoView(this)

        return inferenceInfoView
    }

    override fun onResultsUpdated(results: List<Classifier.Recognition>) {
        tracker.trackResults(results, System.currentTimeMillis())
        trackingOverlay?.postInvalidate()
    }

    override fun onInferenceInfoUpdated(info: InferenceInfo) {
        inferenceInfoView?.setInferenceInfo(info)

        tracker.setFrameConfiguration(
            info.imageSize.width, info.imageSize.height,
            info.imageRotation)
    }

}
