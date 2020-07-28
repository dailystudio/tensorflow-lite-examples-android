package com.dailystudio.tflite.example.common.image

import android.os.Bundle
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import com.dailystudio.devbricksx.camera.CameraFragment
import com.dailystudio.tflite.example.common.Constants
import com.dailystudio.tflite.example.common.InferenceSettings
import com.dailystudio.tflite.example.common.R
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.Notification
import com.rasalexman.kdispatcher.subscribe
import com.rasalexman.kdispatcher.unsubscribe
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbsExampleCameraFragment<Info: ImageInferenceInfo, Results> : CameraFragment() {

    private lateinit var analyzerExecutor: ExecutorService
    protected var analyzer: AbsImageAnalyzer<Info, Results>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyzerExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()

        KDispatcher.subscribe(Constants.EVENT_SETTINGS_CHANGE,
            1, ::eventSettingsUpdateHandler)
    }

    override fun onPause() {
        super.onPause()

        KDispatcher.unsubscribe(Constants.EVENT_SETTINGS_CHANGE,
            ::eventSettingsUpdateHandler)
    }

    override fun buildUseCases(screenAspectRatio: Int, rotation: Int): MutableList<UseCase> {
        val cases = super.buildUseCases(screenAspectRatio, rotation)

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                val analyzer = createAnalyzer(screenAspectRatio, rotation, lensFacing)

                this.analyzer = analyzer
                
                it.setAnalyzer(analyzerExecutor, analyzer)
            }

        cases.add(imageAnalyzer)

        return cases
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example
    }

    abstract fun createAnalyzer(screenAspectRatio: Int,
                                rotation: Int,
                                lensFacing: Int): AbsImageAnalyzer<Info, Results>

    fun getCurrentLensFacing(): Int {
        return lensFacing
    }

    fun changeLensFacing(lensFacing: Int) {
        setCameraLens(lensFacing)
    }

    private fun eventSettingsUpdateHandler(notification: Notification<InferenceSettings>) {
        val analyzer = this.analyzer ?: return

        notification.data?.let {
            analyzer.onInferenceSettingsChange(it)
        }
    }

}