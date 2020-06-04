package com.dailystudio.tflite.example.common

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import com.dailystudio.devbricksx.camera.CameraFragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbsExampleFragment<Results> : CameraFragment() {

    private lateinit var uiThread: Thread

    private lateinit var analyzerExecutor: ExecutorService
    private lateinit var analyzer: AbsExampleAnalyzer<Results>

    private var analysisCallback: AnalysisResultsCallback<Results>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyzerExecutor = Executors.newSingleThreadExecutor()

        uiThread = Thread.currentThread()
    }

    override fun buildUseCases(screenAspectRatio: Int, rotation: Int): MutableList<UseCase> {
        val cases = super.buildUseCases(screenAspectRatio, rotation)

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                analyzer = createAnalyzer(screenAspectRatio, rotation).also { analyzer ->
                    analyzer.addCallback(callbackWrapper)
                }

                it.setAnalyzer(analyzerExecutor, analyzer)
            }

        cases.add(imageAnalyzer)

        return cases
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example
    }

    fun setAnalysisCallback(callback: AnalysisResultsCallback<Results>?) {
        analysisCallback = callback
    }

    override fun onDestroy() {
        super.onDestroy()

        setAnalysisCallback(null)
    }

    abstract fun createAnalyzer(screenAspectRatio: Int, rotation: Int): AbsExampleAnalyzer<Results>

    private val callbackWrapper = object : AnalysisResultsCallback<Results> {

        override fun onResult(results: Results) {

            analysisCallback?.let {
                deliveryToUiThread(results, it)
            }
        }

    }

    private fun deliveryToUiThread(results: Results,
                                   callback: AnalysisResultsCallback<Results>) {
        if (Thread.currentThread() !== uiThread) {
            handler.post{
                callback.onResult(results)
            }
        } else {
            callback.onResult(results)
        }
    }

    private val handler = Handler(Looper.getMainLooper())

}