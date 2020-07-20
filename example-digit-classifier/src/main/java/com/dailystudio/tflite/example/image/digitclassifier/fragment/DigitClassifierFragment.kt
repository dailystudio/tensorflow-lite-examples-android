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
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo
import com.dailystudio.tflite.example.image.digitclassifier.R
import com.divyanshu.draw.widget.DrawView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.digitclassifier.DigitClassifier
import java.io.File

data class RecognizedDigit(val digitBitmap: Bitmap? = null,
                           val digit: Int = -1,
                           val prop: Float = 0f)

class DigitClassifierFragment : Fragment() {

    companion object {
        private const val INFERENCE_IMAGE_FILE = "inference.png"
    }

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        lifecycleScope.launch(Dispatchers.IO) {
            digitClassifier = DigitClassifier(context).also {
                it.initialize()
            }

            inferenceAgent.deliverInferenceInfo(ImageInferenceInfo())
            inferenceAgent.deliverResults(RecognizedDigit())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleScope.launch(Dispatchers.IO) {
            digitClassifier?.close()
        }
    }

    private fun classifyDrawing(bitmap: Bitmap) {
        lifecycleScope.launch {
            val classifier = digitClassifier ?: return@launch

            val info = ImageInferenceInfo()
            info.imageSize = Size(bitmap.width, bitmap.height)
            info.inferenceImageSize = classifier.getInferenceSize()

            val start = System.currentTimeMillis()
            val inferenceBitmap = preProcessBitmap(bitmap, info)
            dumpIntermediateBitmap(inferenceBitmap, INFERENCE_IMAGE_FILE)

            val inferenceStart = System.currentTimeMillis()
            val result = classifier.classify(inferenceBitmap)
            val end = System.currentTimeMillis()

            info.inferenceTime = end - inferenceStart
            info.analysisTime = end - start

            val digit = RecognizedDigit(inferenceBitmap,
                result.first,
                result.second
            )

            inferenceAgent.deliverInferenceInfo(info)
            inferenceAgent.deliverResults(digit)
        }
    }

    private fun preProcessBitmap(frameBitmap: Bitmap,
                                 info: ImageInferenceInfo): Bitmap {
        val matrix = MatrixUtils.getTransformationMatrix(
            frameBitmap.width,  frameBitmap.height,
            480, 480,
            0,
            maintainAspectRatio = true,
            fitIn = true)

        val paddingColor = ResourcesCompatUtils.getColor(
            requireContext(),
            R.color.black)

        return ImageUtils.createTransformedBitmap(frameBitmap,
            matrix, paddingColor = paddingColor)
    }

    private fun dumpIntermediateBitmap(bitmap: Bitmap,
                                       filename: String) {
        if (!isDumpIntermediatesEnabled()) {
            return
        }

        saveIntermediateBitmap(bitmap, filename)
    }

    private fun saveIntermediateBitmap(bitmap: Bitmap,
                                       filename: String) {
        val dir = GlobalContextWrapper.context?.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )

        ImageUtils.saveBitmap(bitmap, File(dir, filename))
    }

    private fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

}