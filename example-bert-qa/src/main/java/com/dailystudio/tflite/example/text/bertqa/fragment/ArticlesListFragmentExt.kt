package com.dailystudio.tflite.example.text.bertqa.fragment

import android.content.ComponentName
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.devbricksx.app.activity.ActivityLauncher
import com.dailystudio.tflite.example.text.bertqa.Article
import com.dailystudio.tflite.example.text.bertqa.ArticleQAActivity

class ArticlesListFragmentExt : ArticlesListFragment() {

    override fun onItemClick(
        recyclerView: RecyclerView,
        itemView: View,
        position: Int,
        item: Article,
        id: Long
    ) {
        super.onItemClick(recyclerView, itemView, position, item, id)

        val context = itemView.context
        val intent = Intent().apply {
            component = ComponentName(context, ArticleQAActivity::class.java)
            putExtra(ArticleQAActivity.EXTRA_ARTICLE_ID, item.id)
        }

        ActivityLauncher.launchActivity(context, intent)
    }
}