package com.dailystudio.tflite.example.common

import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior

abstract class AbsExampleActivity : AppCompatActivity() {

    private var bottomSheetLayout: LinearLayout? = null
    private var visibleLayout: LinearLayout? = null
    private var sheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_example)

        setupViews()
    }

    private fun setupViews() {
        supportFragmentManager.beginTransaction().also {
            val exampleFragment = createExampleFragment()
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

                    val height: Int = it.measuredHeight

                    sheetBehavior?.peekHeight = height
                }
            })

        }

        sheetBehavior?.isHideable = false;

        val titleView: TextView = findViewById(R.id.bottom_sheet_title)
        titleView?.text = title
    }

    abstract fun createExampleFragment(): AbsExampleFragment

}