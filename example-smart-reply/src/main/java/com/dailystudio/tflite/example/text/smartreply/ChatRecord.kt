package com.dailystudio.tflite.example.text.smartreply

import android.graphics.drawable.Drawable
import android.view.View
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsSingleLineViewHolder

enum class Direction {
    Send,
    Receive
}

@ListFragment
@ViewModel
@Adapter(
    viewType = ViewType.SingleLine,
    viewHolder = ChatRecordViewHolder::class)
@InMemoryRepository(key = Long::class)
@InMemoryManager(key = Long::class, ordering = Ordering.Descending)
@DiffUtil
data class ChatRecord(
    val timestamp: Long,
    val text: String,
    val direction: Direction = Direction.Send) : InMemoryObject<Long> {

    override fun getKey(): Long {
        return timestamp
    }

}

class ChatRecordViewHolder(itemView: View): AbsSingleLineViewHolder<ChatRecord>(itemView) {

    override fun getIcon(item: ChatRecord): Drawable? {
        return null
    }

    override fun getText(item: ChatRecord): CharSequence? {
        return item.text
    }

}