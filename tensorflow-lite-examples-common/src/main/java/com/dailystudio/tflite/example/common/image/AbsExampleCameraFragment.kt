package com.dailystudio.tflite.example.common.image

import android.os.Bundle
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import com.dailystudio.devbricksx.camera.CameraFragment
import com.dailystudio.tflite.example.common.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbsExampleCameraFragment<Info: ImageInferenceInfo, Results> : CameraFragment() {

    private lateinit var analyzerExecutor: ExecutorService
    private lateinit var analyzer: AbsImageAnalyzer<Info, Results>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyzerExecutor = Executors.newSingleThreadExecutor()
    }

    override fun buildUseCases(screenAspectRatio: Int, rotation: Int): MutableList<UseCase> {
        val cases = super.buildUseCases(screenAspectRatio, rotation)

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                analyzer = createAnalyzer(screenAspectRatio, rotation, lensFacing)

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

}