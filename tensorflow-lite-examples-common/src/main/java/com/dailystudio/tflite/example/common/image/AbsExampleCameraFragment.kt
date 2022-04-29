package com.dailystudio.tflite.example.common.image

import android.os.Bundle
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import androidx.lifecycle.Observer
import com.dailystudio.devbricksx.camera.CameraFragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.preference.AbsPrefs
import com.dailystudio.devbricksx.preference.PrefsChange
import com.dailystudio.tflite.example.common.R
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.litex.TFLiteModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbsExampleCameraFragment<Model:TFLiteModel, Info: ImageInferenceInfo, Results> : CameraFragment() {

    private lateinit var analyzerExecutor: ExecutorService
    protected var analyzer: AbsImageAnalyzer<Model, Info, Results>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyzerExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()

        val settingsPrefs = getSettingsPreference()
        Logger.debug("[SETTINGS UPDATE]: prefs = $settingsPrefs")
        settingsPrefs.prefsChange.observe(viewLifecycleOwner,
            settingsObserver)
    }

    override fun onPause() {
        super.onPause()

        val settingsPrefs = getSettingsPreference()
        Logger.debug("[SETTINGS UPDATE]: prefs = $settingsPrefs")

        settingsPrefs.prefsChange.removeObserver(settingsObserver)
    }

    protected open fun getSettingsPreference(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

    protected open fun getImageAnalysisBuilder(screenAspectRatio: Int, rotation: Int): ImageAnalysis.Builder {
        return ImageAnalysis.Builder().apply {
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(rotation)
        }
    }

    protected open fun runAnalyzer(image: ImageProxy) {
        this.analyzer?.run(image, getSettingsPreference())
    }

    override fun buildUseCases(screenAspectRatio: Int, rotation: Int): MutableList<UseCase> {
        val cases = super.buildUseCases(screenAspectRatio, rotation)

        val imageAnalyzer = getImageAnalysisBuilder(screenAspectRatio, rotation)
            .build()
            .also {
                val settingsPrefs = getSettingsPreference()

                val analyzer = createAnalyzer(screenAspectRatio, rotation, lensFacing,
                        settingsPrefs.userAverageTime)

                this.analyzer = analyzer
                
                it.setAnalyzer(analyzerExecutor) { image ->
                    runAnalyzer(image)
                }
            }

        cases.add(imageAnalyzer)

        return cases
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example
    }

    abstract fun createAnalyzer(screenAspectRatio: Int,
                                rotation: Int,
                                lensFacing: Int,
                                useAverageTime: Boolean,
    ): AbsImageAnalyzer<Model, Info, Results>

    fun getCurrentLensFacing(): Int {
        return lensFacing
    }

    fun changeLensFacing(lensFacing: Int) {
        setCameraLens(lensFacing)
    }

    private val settingsObserver = Observer<PrefsChange> {
        Logger.debug("[SETTINGS UPDATE]: analyzer = ${this.analyzer}, key = ${it.prefKey}")
        val analyzer = this.analyzer ?: return@Observer

        analyzer.onInferenceSettingsChange(it.prefKey, getSettingsPreference())
    }

    override fun onDestroy() {
        super.onDestroy()

        analyzer?.destroyModel()
        analyzer = null
    }

}