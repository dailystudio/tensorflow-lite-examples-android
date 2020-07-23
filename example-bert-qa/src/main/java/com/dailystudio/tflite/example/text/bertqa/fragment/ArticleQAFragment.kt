package com.dailystudio.tflite.example.text.bertqa.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.fragment.DevBricksFragment
import com.dailystudio.tflite.example.common.InferenceAgent
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.text.bertqa.Article
import com.dailystudio.tflite.example.text.bertqa.Question
import com.dailystudio.tflite.example.text.bertqa.QuestionManager
import com.dailystudio.tflite.example.text.bertqa.R
import com.dailystudio.tflite.example.text.bertqa.model.QuestionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.bertqa.ml.QaAnswer
import org.tensorflow.lite.examples.bertqa.ml.QaClient

class ArticleQAFragment : DevBricksFragment() {

    private var article: Article? = null

    private var contentView: TextView? = null

    private var qaClient: QaClient? = null
    private val inferenceAgent = InferenceAgent<InferenceInfo, List<QaAnswer>>()

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        qaClient = QaClient(context)

        lifecycleScope.launch(Dispatchers.IO) {
            val client = qaClient ?: return@launch

            client.loadModel()
            client.loadDictionary()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleScope.launch(Dispatchers.IO) {
            qaClient?.unload()
        }
    }

    override fun onResume() {
        super.onResume()

        syncUI()
    }

    fun setArticle(article: Article?) {
        this.article = article

        syncUI()
    }

    fun answerQuestion(question: String) {
        val content = article?.content ?: return

        val info = InferenceInfo()

        val start = System.currentTimeMillis()
        val answers = qaClient?.predict(question, content)
        val end = System.currentTimeMillis()
        info.inferenceTime = end - start
        info.analysisTime = info.inferenceTime

        answers?.let {
            inferenceAgent.deliverResults(it)
        }

        inferenceAgent.deliverInferenceInfo(info)
    }

    private fun syncUI() {
        contentView?.text = article?.content ?: ""
    }

}