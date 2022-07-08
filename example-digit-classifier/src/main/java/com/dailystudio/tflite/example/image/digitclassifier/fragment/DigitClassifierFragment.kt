package com.dailystudio.tflite.example.image.digitclassifier.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.image.digitclassifier.DigitClassifierUseCase
import com.dailystudio.tflite.example.image.digitclassifier.R
import com.divyanshu.draw.widget.DrawView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.dailystudio.tensorflow.litex.getLiteUseCaseViewModel

data class RecognizedDigit(val digitBitmap: Bitmap? = null,
                           val digit: Int = -1,
                           val prop: Float = 0f)

class DigitClassifierFragment : Fragment() {

    private var drawView: DrawView? = null
    private var clearButton: View? = null

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

    private fun classifyDrawing(bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            getLiteUseCaseViewModel().performUseCase(DigitClassifierUseCase.UC_NAME, bitmap)
        }
    }

}