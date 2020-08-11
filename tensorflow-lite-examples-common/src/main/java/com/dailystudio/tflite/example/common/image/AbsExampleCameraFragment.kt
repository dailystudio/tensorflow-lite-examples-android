package com.dailystudio.tflite.example.common.image

import android.os.Bundle
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import androidx.lifecycle.Observer
import com.dailystudio.devbricksx.camera.CameraFragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.preference.AbsPrefs
import com.dailystudio.devbricksx.preference.PrefsChange
import com.dailystudio.tflite.example.common.R
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
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

        val settingsPrefs = getSettingsPreference()
        Logger.debug("[CLF UPDATE]: prefs = $settingsPrefs")
        settingsPrefs.prefsChange.observe(viewLifecycleOwner,
            settingsObserver)
    }

    override fun onPause() {
        super.onPause()

        val settingsPrefs = getSettingsPreference()
        Logger.debug("[CLF UPDATE]: prefs = $settingsPrefs")

        settingsPrefs.prefsChange.removeObserver(settingsObserver)
    }

    open protected fun getSettingsPreference(): AbsPrefs {
        return InferenceSettingsPrefs.instance
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

    private val settingsObserver = Observer<PrefsChange> {
        Logger.debug("[CLF UPDATE]: analyzer = ${this.analyzer}, key = ${it.prefKey}")
        val analyzer = this.analyzer ?: return@Observer

        analyzer.onInferenceSettingsChange(it.prefKey)
    }

}