package com.dailystudio.tflite.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.utils.JSONUtils
import com.dailystudio.tflite.example.model.ExampleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch(Dispatchers.IO) {
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
}
