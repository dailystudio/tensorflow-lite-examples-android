package com.dailystudio.tflite.example.text.bertqa

import android.graphics.drawable.Drawable
import android.view.View
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsInformativeCardViewHolder
import com.dailystudio.devbricksx.ui.AbsViewHolder
import com.dailystudio.tflite.example.text.bertqa.fragment.QuestionsListFragmentExt
import com.google.android.material.chip.Chip
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.call

data class Contents(val titles: Array<Array<String>>,
                    val contents: Array<Array<String>>,
                    val questions: Array<Array<String>>)

@ListFragment
@ViewModel
@Adapter(viewType = ViewType.CardInformative,
    viewHolder = ArticleViewHolder::class)
@InMemoryManager(key = Int::class)
@InMemoryRepository(key = Int::class)
@DiffUtil
data class Article(val id: Int,
                   val title: String,
                   val content: String,
                   val questions: Array<String>): InMemoryObject<Int> {

    fun getDisplayTitle(): String {
        val parts = title.split("_")

        return buildString {
            for ((i, p) in parts.withIndex()) {
                append(p.capitalize())
                if (i < parts.size - 1) {
                    append(' ')
                }
            }
        }
    }

    override fun getKey(): Int {
        return id
    }

    override fun toString(): String {
        return buildString {
            append("id = ${id},")
            append("title = ${title},")
            append("content = ${content},")
            append("questions = ${questions},")
        }.replace("%", "%%")
    }

}

class ArticleViewHolder(itemView: View): AbsInformativeCardViewHolder<Article>(itemView) {

    override fun getMedia(item: Article): Drawable? {
        return null
    }

    override fun getTitle(item: Article): CharSequence? {
        return item.getDisplayTitle()
    }

    override fun getSupportingText(item: Article): CharSequence? {
        return item.content
    }

}

@ListFragment(layout = R.layout.fragment_questions_list)
@Adapter(
    layout = R.layout.layout_question,
    viewType = ViewType.Customized,
    viewHolder = QuestionViewHolder::class
)
@ViewModel
@InMemoryRepository(key = Int::class)
@InMemoryManager(key = Int::class)
@DiffUtil
data class Question(val id: Int, val text: String) : InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

}


class QuestionViewHolder(itemView: View): AbsViewHolder<Question>(itemView) {

    override fun bind(item: Question) {
        val clip: Chip? = itemView.findViewById(R.id.chip)
        clip?.text = item.text
        clip?.setOnClickListener {
            KDispatcher.call(QuestionsListFragmentExt.EVENT_QUESTION_SELECTED, item)
        }
    }

}
