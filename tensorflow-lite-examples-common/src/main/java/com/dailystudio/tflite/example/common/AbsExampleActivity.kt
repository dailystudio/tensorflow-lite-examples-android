package com.dailystudio.tflite.example.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class AbsExampleActivity : AppCompatActivity() {

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
    }

    abstract fun createExampleFragment(): AbsExampleFragment

}