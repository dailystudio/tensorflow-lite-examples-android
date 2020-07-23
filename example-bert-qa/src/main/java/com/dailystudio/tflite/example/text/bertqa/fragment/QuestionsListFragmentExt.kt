package com.dailystudio.tflite.example.text.bertqa.fragment

import android.content.ComponentName
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.devbricksx.app.activity.ActivityLauncher
import com.dailystudio.tflite.example.text.bertqa.ArticleQAActivity
import com.dailystudio.tflite.example.text.bertqa.Question

class QuestionsListFragmentExt : QuestionsListFragment() {

    override fun onCreateLayoutManager(): RecyclerView.LayoutManager {
        val layoutManager = super.onCreateLayoutManager()
        if (layoutManager is LinearLayoutManager) {
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        }

        return layoutManager
    }

    override fun onItemClick(
        recyclerView: RecyclerView,
        itemView: View,
        position: Int,
        item: Question,
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