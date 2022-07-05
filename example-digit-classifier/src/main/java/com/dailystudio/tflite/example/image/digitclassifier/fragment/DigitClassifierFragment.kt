package com.dailystudio.tflite.example.image.digitclassifier.fragment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.preference.PrefsChange
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.digitclassifier.DigitClassifierUseCase
import com.dailystudio.tflite.example.image.digitclassifier.R
import com.divyanshu.draw.widget.DrawView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.digitclassifier.DigitClassifier
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.getLiteUseCaseViewModel
import java.io.File

data class RecognizedDigit(val digitBitmap: Bitmap? = null,
                           val digit: Int = -1,
                           val prop: Float = 0f)

class DigitClassifierFragment : Fragment() {

    private var drawView: DrawView? = null
    private var clearButton: View? = null

    private var digitClassifier: DigitClassifier? = null
    private var inferenceAgent: InferenceAgent<InferenceInfo, RecognizedDigit> =
        InferenceAgent()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = LayoutInflater.from(requireContext()).inflate(
            R.layout.fragment_digit_classifier, null)

        setupViews(view)

        return view
    }

    private fun setupViews(fragmentView: View) {
        val context = requireContext()

        val strokeColor = ResourcesCompatUtils.getColor(context,
            R.color.white)

        val canvasColor = ResourcesCompatUtils.getColor(context,
            R.color.black)

        drawView = fragmentView.findViewById(R.id.draw_view)
        drawView?.setStrokeWidth(context.resources.getDimension(R.dimen.draw_stroke_width))
        drawView?.setColor(strokeColor)
        drawView?.setBackgroundColor(canvasColor)
        drawView?.setOnTouchListener { _, event ->
            drawView?.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_UP) {
                val bitmap = drawView?.getBitmap()

                bitmap?.let {
                    classifyDrawing(it)
                }
            }

            true
        }

        clearButton = fragmentView.findViewById(R.id.clear_button)
        clearButton?.setOnClickListener{
            drawView?.clearCanvas()
        }
    }


    override fun onResume() {
        super.onResume()

        val settingsPrefs = getSettingsPreference()
        Logger.debug("[SETTINGS UPDATE]: prefs = $settingsPrefs")
        settingsPrefs.prefsChange.observe(viewLifecycleOwner,
            settingsObserver)
    }

    private fun getSettingsPreference(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

    override fun onPause() {
        super.onPause()

        val settingsPrefs = getSettingsPreference()
        Logger.debug("[SETTINGS UPDATE]: prefs = $settingsPrefs")

        settingsPrefs.prefsChange.removeObserver(settingsObserver)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        lifecycleScope.launch(Dispatchers.IO) {
//            digitClassifier = prepareModel(getSettingsPreference())

            inferenceAgent.deliverInferenceInfo(ImageInferenceInfo())
            inferenceAgent.deliverResults(RecognizedDigit())
        }
    }

    @Synchronized
    private fun prepareModel(settings: InferenceSettingsPrefs): DigitClassifier? {
        val context = GlobalContextWrapper.context
        val model = context?.let {
            val deviceStr = settings.device

            val device = try {
                Model.Device.valueOf(deviceStr)
            } catch (e: Exception) {
                Logger.warn("cannot parse device from [$deviceStr]: $e")

                Model.Device.CPU
            }

            val threads = settings.numberOfThreads

            val useXNNPack = settings.useXNNPack
            Logger.debug("[ANALYZER UPDATE] creating model: device = $device, threads = $threads, useXNNPack = $useXNNPack")

            DigitClassifier(it, device, threads, useXNNPack)
        }

        Logger.debug("[ANALYZER UPDATE] new model created: $model")

        return model
    }

    private fun destroyModel() {
        digitClassifier?.close()
    }

    @Synchronized
    private fun invalidateModel() {
        destroyModel()

        digitClassifier = null
        Logger.debug("[ANALYZER UPDATE] model is invalidated")
    }

    private val settingsObserver = Observer<PrefsChange> {
        when (it.prefKey) {
            InferenceSettingsPrefs.PREF_DEVICE,
            InferenceSettingsPrefs.PREF_NUMBER_OF_THREADS,
            InferenceSettingsPrefs.PREF_USE_X_N_N_PACK -> {
                invalidateModel()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        lifecycleScope.launch(Dispatchers.IO) {
            digitClassifier?.close()
        }
    }

    private fun classifyDrawing(bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            getLiteUseCaseViewModel().performUseCase(DigitClassifierUseCase.UC_NAME, bitmap)
        }
    }

}