package com.dailystudio.tflite.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.AppChangesLiveData
import com.dailystudio.devbricksx.utils.AppUtils
import com.dailystudio.devbricksx.utils.JSONUtils
import com.dailystudio.tflite.example.model.ExampleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXAMPLE_PACKAGE_PREFIX = "com.dailystudio.tflite.example"
    }

    private lateinit var appChangesLiveData: AppChangesLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        appChangesLiveData = AppChangesLiveData(this).apply {
            observe(this@MainActivity, Observer {
                lifecycleScope.launch{
                    if (packageName.startsWith(EXAMPLE_PACKAGE_PREFIX)) {
                        checkExampleInstallation(it.packageName)
                    }
                }
            })
        }

        runBlocking {
            createExamples()
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            checkExampleInstallations()
        }
    }

    private fun findExampleByPackageName(packageName: String): Example? {
        val viewModel = ViewModelProvider(this@MainActivity)
            .get(ExampleViewModel::class.java)

        val examples = viewModel.allExamples
        for (example in examples) {
            if (example.`package` != null && example.`package` == packageName) {
                return example
            }
        }

        return null
    }

    private fun updateExampleInstallation(example: Example) {
        val viewModel = ViewModelProvider(this@MainActivity)
            .get(ExampleViewModel::class.java)

        Logger.debug("example: $example")
        example.installed = if (example.`package` == null) {
            false
        } else {
            AppUtils.isApplicationInstalled(this@MainActivity, example.`package`)
        }

        viewModel.updateExample(example)
    }

    private fun checkExampleInstallation(packageName: String) {
        val example = findExampleByPackageName(packageName)

        example?.let {
            updateExampleInstallation(example)
        }
    }

    private fun checkExampleInstallations() {
        val viewModel = ViewModelProvider(this@MainActivity)
            .get(ExampleViewModel::class.java)

        val examples = viewModel.allExamples
        for (example in examples) {
            updateExampleInstallation(example)
        }
    }

    private fun createExamples() {
        val examples = JSONUtils.fromRaw(
            this@MainActivity, R.raw.examples, Array<Example>::class.java)

        examples?.let {
            val viewModel = ViewModelProvider(this@MainActivity)
                .get(ExampleViewModel::class.java)

            for (example in it) {
                viewModel.insertExample(example)
            }
        }
    }

}
