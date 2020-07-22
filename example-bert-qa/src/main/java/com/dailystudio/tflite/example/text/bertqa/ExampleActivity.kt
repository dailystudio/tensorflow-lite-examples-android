package com.dailystudio.tflite.example.text.bertqa

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.app.activity.DevBricksActivity
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.JSONUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExampleActivity : DevBricksActivity() {

    companion object {
        const val CONTENTS_FILE = "contents_from_squad.json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_articles)

        lifecycleScope.launch(Dispatchers.IO) {
            val contents = JSONUtils.fromAsset(
                this@ExampleActivity,
                CONTENTS_FILE,
                Contents::class.java)

            Logger.debug("contents: $contents")
        }
    }

}