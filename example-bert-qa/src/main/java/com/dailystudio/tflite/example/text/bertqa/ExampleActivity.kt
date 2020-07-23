package com.dailystudio.tflite.example.text.bertqa

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.app.activity.DevBricksActivity
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.JSONUtils
import com.dailystudio.tflite.example.text.bertqa.model.ArticleViewModel
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
            loadContents()
        }
    }

    private fun loadContents() {
        val contents = JSONUtils.fromAsset(
            this@ExampleActivity,
            CONTENTS_FILE,
            Contents::class.java) ?: return

        Logger.debug("contents: $contents")

        val viewModel = ViewModelProvider(this).get(ArticleViewModel::class.java)

        for ((index, title) in contents.titles.withIndex()) {
            val article = Article(index,
                title[0],
                contents.contents[index][0],
                contents.questions[index])

            viewModel.insertArticle(article)
        }
    }

}