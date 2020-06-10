package com.dailystudio.tflite.example.common

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dailystudio.devbricksx.development.Logger
import com.google.android.material.bottomsheet.BottomSheetBehavior

abstract class AbsExampleActivity<Info: InferenceInfo, Results> : AppCompatActivity() {

    private var bottomSheetLayout: ViewGroup? = null
    private var visibleLayout: ViewGroup? = null
    private var divider: View? = null
    private var hiddenLayout: ViewGroup? = null
    private var sheetBehavior: BottomSheetBehavior<ViewGroup>? = null

    private var resultsView: View? = null
    private var hiddenView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getLayoutResId())

        setupViews()
    }

    private fun setupViews() {
        supportFragmentManager.beginTransaction().also {
            val exampleFragment = createExampleFragment().also { fragment ->
                fragment.setAnalysisCallback(analysisCallback, analysisCallback)
            }

            it.add(R.id.fragment_stub, exampleFragment, "example-fragment")
            it.show(exampleFragment)
            it.commitAllowingStateLoss()
        }

        applyBottomSheetFeatures()
    }

    private fun applyBottomSheetFeatures() {
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout)
        bottomSheetLayout?.let {
            sheetBehavior = BottomSheetBehavior.from(it)
        }

        visibleLayout = findViewById(R.id.visible_layout)
        visibleLayout?.let{
            val vto: ViewTreeObserver = it.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    var padding = 0
                    bottomSheetLayout?.let { sheet ->
                        padding += sheet.paddingTop
                        padding += sheet.paddingBottom
                    }

                    val height: Int = it.height + padding

                    sheetBehavior?.peekHeight = height
                }
            })
        }

        divider = findViewById(R.id.bottom_sheet_divider)

        sheetBehavior?.isHideable = false;

        val titleView: TextView = findViewById(R.id.bottom_sheet_title)
        titleView?.text = title

        val resultContainer: ViewGroup = findViewById(R.id.bottom_sheet_result)
        resultContainer?.let {
            resultsView = createResultsView()

            if (resultsView == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                it.addView(resultsView)
            }
        }

        hiddenLayout = findViewById(R.id.hidden_layout)
        hiddenLayout?.let {
            hiddenView = createHiddenView()

            if (hiddenView == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                it.addView(hiddenView)
            }
        }

        divider?.let {
            it.visibility = if (resultsView == null && hiddenView == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    protected open fun getLayoutResId(): Int {
        return R.layout.activity_example
    }

    protected open fun getResultsUpdateInterval(): Long {
        return 0L
    }

    abstract fun createExampleFragment(): AbsExampleFragment<Info, Results>
    abstract fun createResultsView(): View?
    abstract fun createHiddenView(): View?
    abstract fun onResultsUpdated(results: Results)
    abstract fun onInferenceInfoUpdated(info: Info)

    private val analysisCallback = object: ResultsCallback<Results>, InferenceCallback<Info> {

        override fun onResult(results: Results) {
            Logger.debug("latest result: $results")

            onResultsUpdated(results)
        }

        override fun onInference(info: Info) {
            Logger.debug("latest info: $info")

            onInferenceInfoUpdated(info)
        }

    }


}