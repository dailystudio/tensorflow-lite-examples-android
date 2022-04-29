package com.dailystudio.tflite.example.text.bertqa.fragment

import android.content.ComponentName
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.devbricksx.app.activity.ActivityLauncher
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.text.bertqa.ArticleQAActivity
import com.dailystudio.tflite.example.text.bertqa.Question
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.call

class QuestionsListFragmentExt : QuestionsListFragment() {

    companion object {
        const val EVENT_QUESTION_SELECTED = "question-selected"
    }

    override fun onCreateLayoutManager(): RecyclerView.LayoutManager? {
        val layoutManager = super.onCreateLayoutManager()
        if (layoutManager is LinearLayoutManager) {
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        }

        return layoutManager
    }

}