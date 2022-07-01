package com.dailystudio.tflite.example.video.classification.fragment

import android.hardware.camera2.CaptureRequest
import android.util.Range
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.ImageAnalysis
import com.dailystudio.tflite.example.video.classification.VideoClassificationUseCase
import org.tensorflow.litex.fragment.LiteCameraUseCaseFragment

class VideoClassificationCameraFragment : LiteCameraUseCaseFragment() {

    override fun getImageAnalysisBuilder(
        screenAspectRatio: Int,
        rotation: Int
    ): ImageAnalysis.Builder {
        val builder = super.getImageAnalysisBuilder(screenAspectRatio, rotation)

        builder.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

        val captureFps = VideoClassificationUseCase.MAX_CAPTURE_FPS
        val modelFps = VideoClassificationUseCase.MODEL_FPS
        val targetFpsMultiplier = captureFps.div(modelFps)
        val targetCaptureFps = modelFps * targetFpsMultiplier

        val extender: Camera2Interop.Extender<*> =
            Camera2Interop.Extender(builder)
        extender.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
            Range(targetCaptureFps, targetCaptureFps)
        )

        return builder
    }

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(VideoClassificationUseCase.UC_NAME)

}