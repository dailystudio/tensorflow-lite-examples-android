package com.dailystudio.tflite.example.image.pose

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import org.tensorflow.litex.InferenceInfo
import org.tensorflow.litex.image.ImageInferenceInfo
import com.dailystudio.tflite.example.image.pose.fragment.PoseCameraFragment
import com.dailystudio.tflite.example.image.pose.ui.PoseOverlayView
import org.tensorflow.lite.examples.posenet.lib.Person
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.activity.LiteUseCaseActivity

class ExampleActivity : LiteUseCaseActivity() {

    private var poseOverlay: PoseOverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        poseOverlay =
            findViewById(R.id.overlay)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_pose
    }

    override fun createBaseFragment(): Fragment {
        return PoseCameraFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        when (nameOfUseCase) {
            PoseUseCase.UC_NAME -> {
                Logger.debug("detected pose: $results")
                poseOverlay?.setPersonPose(results as Person)
            }
        }
    }

    override fun onInferenceInfoUpdated(nameOfUseCase: String, info: InferenceInfo) {
        super.onInferenceInfoUpdated(nameOfUseCase, info)

        when (nameOfUseCase) {
            PoseUseCase.UC_NAME -> {
                val imageInferenceInfo = info as ImageInferenceInfo
                poseOverlay?.setFrameConfiguration(
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
            PoseUseCase.UC_NAME to PoseUseCase()
        )
    }

}