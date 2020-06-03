package com.dailystudio.tflite.example.common

import android.os.Bundle
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import com.dailystudio.devbricksx.camera.CameraFragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbsExampleFragment : CameraFragment() {

    private lateinit var analyzerExecutor: ExecutorService

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
                it.setAnalyzer(analyzerExecutor, createAnalyzer(screenAspectRatio, rotation))
            }

        cases.add(imageAnalyzer)

        return cases
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example
    }

    abstract fun createAnalyzer(screenAspectRatio: Int, rotation: Int): ImageAnalysis.Analyzer

}