package com.dailystudio.tflite.example.common

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import com.dailystudio.devbricksx.camera.CameraFragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbsExampleFragment<Info: InferenceInfo, Results> : CameraFragment() {

    private lateinit var uiThread: Thread

    private lateinit var analyzerExecutor: ExecutorService
    private lateinit var analyzer: AbsExampleAnalyzer<Info, Results>

    private var resultsCallback: ResultsCallback<Results>? = null
    private var inferenceCallback: InferenceCallback<Info>? = null

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
                    analyzer.addInferenceCallback(callbackWrapper)
                    analyzer.addResultsCallback(callbackWrapper)
                }

                it.setAnalyzer(analyzerExecutor, analyzer)
            }

        cases.add(imageAnalyzer)

        return cases
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example
    }

    fun setAnalysisCallback(inferenceCallback: InferenceCallback<Info>?,
                            resultsCallback: ResultsCallback<Results>?) {
        this.inferenceCallback = inferenceCallback
        this.resultsCallback = resultsCallback
    }

    override fun onDestroy() {
        super.onDestroy()

        setAnalysisCallback(null, null)
    }

    abstract fun createAnalyzer(screenAspectRatio: Int,
                                rotation: Int): AbsExampleAnalyzer<Info, Results>

    private val callbackWrapper = object : ResultsCallback<Results>, InferenceCallback<Info> {

        override fun onResult(results: Results) {
            resultsCallback?.let {
                deliveryResultToUiThread(results, it)
            }
        }

        override fun onInference(info: Info) {
            inferenceCallback?.let {
                deliveryInferenceInfoToUiThread(info, it)
            }
        }

    }

    private fun deliveryInferenceInfoToUiThread(info: Info,
                                                callback: InferenceCallback<Info>) {
        if (Thread.currentThread() !== uiThread) {
            handler.post{
                callback.onInference(info)
            }
        } else {
            callback.onInference(info)
        }
    }

    private fun deliveryResultToUiThread(results: Results,
                                         callback: ResultsCallback<Results>) {
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