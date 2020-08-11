package com.dailystudio.tflite.example.text.bertqa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.text.bertqa.fragment.ArticleQAFragment
import com.dailystudio.tflite.example.text.bertqa.fragment.QuestionsListFragmentExt
import com.dailystudio.tflite.example.text.bertqa.model.ArticleViewModel
import com.dailystudio.tflite.example.text.bertqa.model.QuestionViewModel
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.Notification
import com.rasalexman.kdispatcher.subscribe
import com.rasalexman.kdispatcher.unsubscribe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.bertqa.ml.QaAnswer

class ArticleQAActivity : AbsExampleActivity<InferenceInfo, List<QaAnswer>>() {

    companion object {

        const val EXTRA_ARTICLE_ID = "article-id"

    }

    private var article: Article? = null
    private var qaFragment: ArticleQAFragment? = null

    private var userInput: EditText? = null
    private var sendButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userInput = findViewById(R.id.input)
        userInput?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton?.isEnabled = !(s == null || s.isEmpty())
            }
        })

        sendButton = findViewById(R.id.send_button)
        sendButton?.setOnClickListener{
            val editable = userInput?.text ?: return@setOnClickListener
            val text = editable.toString()

            askQuestion(text)
        }
    }

    override fun onResume() {
        super.onResume()

        parseArticle(intent)

        KDispatcher.subscribe(QuestionsListFragmentExt.EVENT_QUESTION_SELECTED,
            1, ::questionHandler)
    }

    override fun onPause() {
        super.onPause()

        KDispatcher.unsubscribe(QuestionsListFragmentExt.EVENT_QUESTION_SELECTED,
            ::questionHandler)
    }

    private fun clearInputFocus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)

        val focusView = currentFocus
        focusView?.clearFocus()
    }

    private fun parseArticle(intent: Intent) {
        val articleId = intent.getIntExtra(EXTRA_ARTICLE_ID, -1)
        Logger.debug("article id: $articleId")
        if (articleId == -1) {
            return
        }

        val viewModel = ViewModelProvider(this).get(ArticleViewModel::class.java)

        article = viewModel.getArticle(articleId)
        Logger.debug("open article: $article")

        setExampleTitle(article?.getDisplayTitle() ?: title)

        qaFragment?.setArticle(article)

        lifecycleScope.launch(Dispatchers.IO) {
            reloadQuestions()
        }
    }

    override fun createBaseFragment(): Fragment {
        val fragment = ArticleQAFragment()

        qaFragment = fragment

        return fragment
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: List<QaAnswer>) {
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_article_qa
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

    private fun askQuestion(question: String) {
        var refinedQuestion = question.trim()
        if (refinedQuestion.isEmpty()) {
            userInput?.setText(refinedQuestion)

            return
        }

        if (!refinedQuestion.endsWith("?")) {
            refinedQuestion += '?'
        }
        userInput?.setText(refinedQuestion)

        clearInputFocus()

        lifecycleScope.launch(Dispatchers.IO) {
            qaFragment?.answerQuestion(refinedQuestion)
        }

    }

    private fun questionHandler(notification: Notification<Question>){
        Logger.debug("new question selected: ${notification.data}")
        when(notification.eventName){
            QuestionsListFragmentExt.EVENT_QUESTION_SELECTED -> {
                val question = notification.data

                question?.let {
                    askQuestion(it.text)
                }
            }
        }
    }

    override fun createSettingsFragment(): AbsSettingsDialogFragment? {
        TODO("Not yet implemented")
    }
}