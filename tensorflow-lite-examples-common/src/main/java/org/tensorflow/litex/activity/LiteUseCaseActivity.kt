package org.tensorflow.litex.activity

import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.app.activity.DevBricksActivity
import com.dailystudio.devbricksx.fragment.AbsAboutFragment
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import org.tensorflow.litex.InferenceInfo
import com.dailystudio.tflite.example.common.R
import org.tensorflow.litex.ui.InferenceInfoView
import org.tensorflow.litex.fragment.InferenceSettingsFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.getLiteUseCaseViewModel
import org.tensorflow.litex.observeUseCaseInfo
import org.tensorflow.litex.observeUseCaseOutput

abstract class LiteUseCaseActivity: DevBricksActivity() {

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
    private var inferenceInfoViews: MutableMap<String, InferenceInfoView> = mutableMapOf()

    private var settingsFragment: AbsSettingsDialogFragment? = null
    lateinit var exampleFragment: Fragment

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
            exampleFragment = createBaseFragment()

            it.add(R.id.fragment_stub, exampleFragment, "example-fragment")
            it.show(exampleFragment)
            it.commitAllowingStateLoss()
        }

        val nameOfUseCases = prepareLiteUseCase()

        applyBottomSheetFeatures(nameOfUseCases)
        applyOverflowMenus()
    }

    private fun prepareLiteUseCase(): Array<String> {
        val viewModel = getLiteUseCaseViewModel()
        val useCases = buildLiteUseCase()
        useCases.forEach { entry ->
            val name = entry.key
            val useCase = entry.value

            viewModel.manageUseCase(name, useCase)
            observeUseCaseOutput(name) { output ->
                output?.let {
                    updateResultsOnUiThread(name, it)
                }
            }
            observeUseCaseInfo(name) { info ->
                updateInferenceInfoToUiThread(name, info)
            }
        }

        return useCases.keys.toTypedArray()
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

    private fun applyBottomSheetFeatures(namesOfUseCases: Array<String>) {
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
            for (name in namesOfUseCases) {
                val inferenceInfoView = createInferenceInfoView(name)
                if (inferenceInfoView != null) {
                    it.addView(inferenceInfoView)
                    inferenceInfoViews[name] = inferenceInfoView
                }
            }

            if (inferenceInfoViews.isEmpty()) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }

        divider?.let {
            it.visibility = if (resultsView == null
                && inferenceInfoViews.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        expandIndicator = findViewById(R.id.bottom_sheet_expand_indicator)
        expandIndicator?.let {
            if (inferenceInfoViews.isEmpty()) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }

        sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    protected open fun getLayoutResId(): Int {
        return R.layout.activity_example
    }

    protected open fun createInferenceInfoView(nameOfUseCase: String): InferenceInfoView? {
        return InferenceInfoView(this)
    }

    protected open fun onInferenceInfoUpdated(nameOfUseCase: String, info: InferenceInfo) {
        inferenceInfoViews[nameOfUseCase]?.setInferenceInfo(info)
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

    abstract fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>>
    abstract fun createBaseFragment(): Fragment
    abstract fun createResultsView(): View?
    abstract fun onResultsUpdated(nameOfUseCase: String, results: Any)

    private fun updateInferenceInfoToUiThread(nameOfUseCase: String, info: InferenceInfo) {
        lifecycleScope.launch(Dispatchers.Main) {
            onInferenceInfoUpdated(nameOfUseCase, info)
        }
    }

    private fun updateResultsOnUiThread(nameOfUseCase: String, results: Any) {
        lifecycleScope.launch(Dispatchers.Main) {
            onResultsUpdated(nameOfUseCase, results)
        }
    }

}