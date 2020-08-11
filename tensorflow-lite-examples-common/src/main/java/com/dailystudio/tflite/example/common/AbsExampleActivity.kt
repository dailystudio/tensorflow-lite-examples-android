package com.dailystudio.tflite.example.common

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.app.activity.DevBricksActivity
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.AbsAboutFragment
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.common.ui.InferenceInfoView
import com.dailystudio.tflite.example.common.ui.InferenceSettingsFragment
import com.dailystudio.tflite.example.common.utils.ResultsUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.Notification
import com.rasalexman.kdispatcher.subscribe
import com.rasalexman.kdispatcher.unsubscribe

abstract class AbsExampleActivity<Info: InferenceInfo, Results> : DevBricksActivity() {

    class AboutFragment(private val exampleName: CharSequence?,
                        private val exampleIconResId: Int,
                        private val exampleDesc: CharSequence?,
                        private val exampleThumbResId: Int) : AbsAboutFragment() {
        override val appThumbResource: Int
            get() = exampleThumbResId

        override val appName: CharSequence?
            get() = exampleName

        override val appDescription: CharSequence?
            get() = exampleDesc

        override val appIconResource: Int
            get() = exampleIconResId

    }

    private var bottomSheetLayout: ViewGroup? = null
    private var visibleLayout: ViewGroup? = null
    private var divider: View? = null
    private var hiddenLayout: ViewGroup? = null
    private var sheetBehavior: BottomSheetBehavior<ViewGroup>? = null
    private var expandIndicator: ImageView? = null
    private var titleView: TextView? = null

    private var resultsView: View? = null
    private var inferenceInfoView: InferenceInfoView? = null

    private var settingsFragment: AbsSettingsDialogFragment? = null

    private lateinit var uiThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getLayoutResId())

        setupViews()

        uiThread = Thread.currentThread()

        if (shouldKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_example_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                val fragment = createAboutFragment()

                fragment.show(supportFragmentManager, "about")
            }

            R.id.action_settings -> {
                val fragment = settingsFragment ?: createSettingsFragment()

                settingsFragment = fragment


                settingsFragment?.show(supportFragmentManager, "settings")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    protected open fun setupViews() {
        supportFragmentManager.beginTransaction().also {
            val exampleFragment = createBaseFragment()

            it.add(R.id.fragment_stub, exampleFragment, "example-fragment")
            it.show(exampleFragment)
            it.commitAllowingStateLoss()
        }

        applyBottomSheetFeatures()
        applyOverflowMenus()
    }

    private fun applyOverflowMenus() {
        val overflowMenu: View? = findViewById(R.id.overflow_menu)
        overflowMenu?.setOnClickListener {
            val popup = PopupMenu(this, it)
            val inflater: MenuInflater = popup.menuInflater

            inflater.inflate(R.menu.menu_example_activity, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                onOptionsItemSelected(menuItem)
            }

            popup.show()
        }
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
//                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)

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

        titleView = findViewById(R.id.bottom_sheet_title)
        setExampleTitle(title)

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

            if (inferenceInfoView == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }

        divider?.let {
            it.visibility = if (resultsView == null
                && inferenceInfoView == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        expandIndicator = findViewById(R.id.bottom_sheet_expand_indicator)
        expandIndicator?.let {
            if (inferenceInfoView == null) {
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

    protected open fun shouldKeepScreenOn(): Boolean {
        return true
    }

    protected open fun getExampleTitle(): CharSequence {
        return titleView?.text ?: title
    }

    protected open fun setExampleTitle(title: CharSequence) {
        titleView?.text = title
    }

    protected open fun createAboutFragment(): AbsAboutFragment {
        return AboutFragment(
            getExampleName(),
            getExampleIconResource(),
            getExampleDesc(),
            getExampleThumbResource())
    }

    protected open fun getExampleThumbResource(): Int {
        return R.drawable.app_thumb
    }

    protected open fun getExampleIconResource(): Int {
        return R.mipmap.ic_launcher
    }

    protected open fun getExampleName(): CharSequence? {
        return getString(R.string.default_app_name)
    }

    protected open fun getExampleDesc(): CharSequence? {
        return getString(R.string.default_example_desc)
    }

    protected open fun createSettingsFragment(): AbsSettingsDialogFragment? {
        return InferenceSettingsFragment()
    }

    abstract fun createBaseFragment(): Fragment
    abstract fun createResultsView(): View?
    abstract fun onResultsUpdated(results: Results)

    @Suppress("UNCHECKED_CAST")
    private fun eventResultsUpdateHandler(notification: Notification<Any>) {
        val data = notification.data ?: return

        val results = data as Results
        Logger.debug("latest result: ${ResultsUtils.safeToPrintableLog(results)}")

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