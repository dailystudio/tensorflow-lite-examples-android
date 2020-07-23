package com.dailystudio.tflite.example.text.bertqa.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.fragment.DevBricksFragment
import com.dailystudio.tflite.example.text.bertqa.Article
import com.dailystudio.tflite.example.text.bertqa.Question
import com.dailystudio.tflite.example.text.bertqa.QuestionManager
import com.dailystudio.tflite.example.text.bertqa.R
import com.dailystudio.tflite.example.text.bertqa.model.QuestionViewModel

class ArticleQAFragment : DevBricksFragment() {

    private var article: Article? = null

    private var contentView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_qa, null)

        setupViews(view)

        return view
    }

    private fun setupViews(fragmentView: View) {
        contentView = fragmentView.findViewById(R.id.article_content)
    }

    override fun onResume() {
        super.onResume()

        syncUI()
    }

    fun setArticle(article: Article?) {
        this.article = article

        syncUI()
    }

    private fun syncUI() {
        contentView?.text = article?.content ?: ""

        reloadQuestions()
    }

    private fun reloadQuestions() {
        val viewModel = ViewModelProvider(this).get(QuestionViewModel::class.java)

        QuestionManager.clear()

        val questions = article?.questions ?: return

        for ((i, q) in questions.withIndex()) {
            val question = Question(i, q)

            viewModel.insertQuestion(question)
        }
    }

}