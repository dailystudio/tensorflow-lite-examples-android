package com.dailystudio.tflite.example.speech.recognition

import android.graphics.drawable.Drawable
import android.view.View
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsSingleLineViewHolder


@Adapter(
    viewHolder = CommandViewHolder::class,
    viewType = ViewType.SingleLine
)
@ListFragment(gridLayout = true, columns = 2, layout = R.layout.fragment_commands_list)
@DiffUtil
@ViewModel
@InMemoryRepository(key = String::class)
@InMemoryManager(key = String::class)
data class Command(val label: String,
                   var prop: Float = 0f): InMemoryObject<String> {

    override fun getKey(): String {
        return label
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Command) {
            false
        } else {
            (label == other.label && prop == other.prop)
        }
    }

}

class CommandViewHolder(itemView: View): AbsSingleLineViewHolder<Command>(itemView) {

    override fun getIcon(item: Command): Drawable? {
        return null
    }

    override fun getText(item: Command): CharSequence? {
        return if (item.prop > 0f) {
            buildString {
                append("${item.label.capitalize()}")
                append("(")
                append("%2.1f%%".format(item.prop * 100))
                append(")")
            }
        } else {
            return item.label.capitalize()
        }
    }

}