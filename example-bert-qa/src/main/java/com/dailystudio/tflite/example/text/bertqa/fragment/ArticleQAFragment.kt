package com.dailystudio.tflite.example.text.bertqa.fragment

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.DevBricksFragment
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.text.bertqa.Article
import com.dailystudio.tflite.example.text.bertqa.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.bertqa.ml.QaAnswer
import org.tensorflow.lite.examples.bertqa.ml.QaClient
import org.tensorflow.lite.examples.bertqa.ml.QaUseCase
import org.tensorflow.lite.support.model.Model

class ArticleQAFragment : DevBricksFragment() {

    private var article: Article? = null

    private var contentView: TextView? = null

//    private var qaClient: QaClient? = null

    private var useCase: QaUseCase? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_qa, null)

        setupViews(view)

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useCase = QaUseCase(this)
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

    suspend fun answerQuestion(question: String) {
        val content = article?.content ?: return

        withContext(Dispatchers.Main) {
            contentView?.text = content
        }

        val answers = useCase?.run(Pair(question, content))

        answers?.let {
            if (answers.isNotEmpty()) {
                val topAnswer = answers[0]

                withContext(Dispatchers.Main) {
                    presentAnswer(topAnswer)
                }
            }
        }
    }

    private fun presentAnswer(answer: QaAnswer) {
        val content = article?.content ?: return
        val context = requireContext() ?: return

        val highlightColor = ResourcesCompatUtils.getColor(
            context, R.color.colorPrimary)

        val spanText: Spannable = SpannableString(content)
        val offset: Int = content.indexOf(answer.text, 0)
        if (offset >= 0) {
            spanText.setSpan(
                BackgroundColorSpan(highlightColor),
                offset,
                offset + answer.text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        contentView?.text = spanText
    }


    private fun syncUI() {
        contentView?.text = article?.content ?: ""
    }

}