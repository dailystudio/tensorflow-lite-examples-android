package com.dailystudio.tflite.example.image.digitclassifier

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.tflite.example.image.digitclassifier.fragment.DigitClassifierFragment
import com.dailystudio.tflite.example.image.digitclassifier.fragment.RecognizedDigit
import com.dailystudio.tensorflow.litex.LiteUseCase
import com.dailystudio.tensorflow.litex.activity.LiteUseCaseActivity

class ExampleActivity : LiteUseCaseActivity() {

    private var digitBitmap: ImageView? = null
    private var resultDigit: TextView? = null
    private var resultProp: TextView? = null

    override fun createBaseFragment(): Fragment {
        return DigitClassifierFragment()
    }

    override fun createResultsView(): View? {
        val view: View = LayoutInflater.from(this).inflate(
            R.layout.layout_results_view, null)

        digitBitmap = view.findViewById(R.id.result_image)
        resultDigit = view.findViewById(R.id.result_digit)
        resultProp = view.findViewById(R.id.result_prop)

        return view
    }

    override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
        Logger.debug("result: $results")

        if (results is RecognizedDigit) {
            digitBitmap?.setImageBitmap(if (results.digitBitmap == null) {
                null
            } else {
                val radius = resources.getDimensionPixelSize(
                    R.dimen.results_image_round_corner_radius).toFloat()
                ImageUtils.clipBitmapWithRoundCorner(results.digitBitmap, radius)
            })

            resultDigit?.text = if (results.digit != -1) {
                "%d".format(results.digit)
            } else {
                getString(R.string.prompt_draw)
            }

            resultProp?.text = if (results.digit != -1) {
                "(%3.1f%%)".format(results.prop * 100)
            } else {
                ""
            }
        }
    }

    override fun getExampleName(): CharSequence? {
        return getString(R.string.app_name)
    }

    override fun getExampleIconResource(): Int {
        return R.drawable.about_icon
    }

    override fun getExampleDesc(): CharSequence? {
        return getString(R.string.app_desc)
    }

    override fun getExampleThumbVideoResource(): Int {
        return R.raw.digit_classifier_720p
    }

    override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
        return mapOf(
            DigitClassifierUseCase.UC_NAME to DigitClassifierUseCase()
        )
    }

}