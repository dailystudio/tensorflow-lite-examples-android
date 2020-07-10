package com.dailystudio.tflite.example.common

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.ui.InferenceInfoView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.Notification
import com.rasalexman.kdispatcher.subscribe
import com.rasalexman.kdispatcher.unsubscribe

abstract class AbsExampleActivity<Info: InferenceInfo, Results> : AppCompatActivity() {

    private var bottomSheetLayout: ViewGroup? = null
    private var visibleLayout: ViewGroup? = null
    private var divider: View? = null
    private var hiddenLayout: ViewGroup? = null
    private var sheetBehavior: BottomSheetBehavior<ViewGroup>? = null
    private var expandIndicator: ImageView? = null

    private var resultsView: View? = null
    private var inferenceInfoView: InferenceInfoView? = null
    private var settingsView: View? = null

    private lateinit var uiThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getLayoutResId())

        setupViews()

        uiThread = Thread.currentThread()
    }

    internal fun setupViews() {
        supportFragmentManager.beginTransaction().also {
            val exampleFragment = createBaseFragment()

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
            sheetBehavior?.addBottomSheetCallback(object : BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN, BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            expandIndicator?.setImageResource(R.drawable.ic_arrow_down)
                        }
                        BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_SETTLING -> {
                            expandIndicator?.setImageResource(R.drawable.ic_arrow_up)
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
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

        sheetBehavior?.isHideable = false

        val titleView: TextView? = findViewById(R.id.bottom_sheet_title)
        titleView?.text = title

        val resultContainer: ViewGroup? = findViewById(R.id.bottom_sheet_result)
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
            inferenceInfoView = createInferenceInfoView()
            if (inferenceInfoView != null) {
                it.addView(inferenceInfoView)
            }

            settingsView = createSettingsView()
            if (settingsView != null) {
                it.addView(settingsView)
            }

            if (inferenceInfoView == null && settingsView == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }

        divider?.let {
            it.visibility = if (resultsView == null
                && inferenceInfoView == null
                && settingsView == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        expandIndicator = findViewById(R.id.bottom_sheet_expand_indicator)
        expandIndicator?.let {
            if (inferenceInfoView == null && settingsView == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        KDispatcher.subscribe(Constants.EVENT_INFERENCE_INFO_UPDATE,
            1, ::eventInferenceInfoUpdateHandler)

        KDispatcher.subscribe(Constants.EVENT_RESULTS_UPDATE,
            1, ::eventResultsUpdateHandler)
    }

    override fun onStop() {
        super.onStop()

        KDispatcher.unsubscribe(Constants.EVENT_INFERENCE_INFO_UPDATE,
            ::eventInferenceInfoUpdateHandler)

        KDispatcher.unsubscribe(Constants.EVENT_RESULTS_UPDATE,
            ::eventResultsUpdateHandler)
    }

    protected open fun getLayoutResId(): Int {
        return R.layout.activity_example
    }

    protected open fun createInferenceInfoView(): InferenceInfoView? {
        return InferenceInfoView(this)
    }

    protected open fun onInferenceInfoUpdated(info: Info) {
        inferenceInfoView?.setInferenceInfo(info)
    }

    abstract fun createBaseFragment(): Fragment
    abstract fun createResultsView(): View?
    abstract fun createSettingsView(): View?
    abstract fun onResultsUpdated(results: Results)

    @Suppress("UNCHECKED_CAST")
    private fun eventResultsUpdateHandler(notification: Notification<Any>) {
        val data = notification.data ?: return

        val results = data as Results
        Logger.debug("latest result: ${results.toString().replace("%", "%%")}")

        updateResultsOnUiThread(results)
    }

    @Suppress("UNCHECKED_CAST")
    private fun eventInferenceInfoUpdateHandler(notification: Notification<Any>) {
        val data = notification.data ?: return

        val info = data as Info
        Logger.debug("latest info: $info")

        updateInferenceInfoToUiThread(info)
    }

    private fun updateInferenceInfoToUiThread(info: Info) {
        if (Thread.currentThread() !== uiThread) {
            handler.post{
                onInferenceInfoUpdated(info)
            }
        } else {
            onInferenceInfoUpdated(info)
        }
    }

    private fun updateResultsOnUiThread(results: Results) {
        if (Thread.currentThread() !== uiThread) {
            handler.post{
                onResultsUpdated(results)
            }
        } else {
            onResultsUpdated(results)
        }
    }

    private val handler = Handler(Looper.getMainLooper())


}