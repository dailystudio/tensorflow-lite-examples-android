package com.dailystudio.tflite.example.text.smartreply

import android.view.Gravity
import android.view.View
import android.widget.*
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.annotations.Adapter
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsViewHolder

enum class Direction {
    Send,
    Receive
}

@ListFragment
@ViewModel
@Adapter(
    layout = R.layout.layout_chat_record,
    viewType = ViewType.Customized,
    viewHolder = ChatRecordViewHolder::class)
@InMemoryRepository(key = Long::class)
@InMemoryManager(key = Long::class, ordering = Ordering.Ascending)
@DiffUtil
data class ChatRecord(
    val timestamp: Long,
    val text: String,
    val direction: Direction = Direction.Send) : InMemoryObject<Long> {

    override fun getKey(): Long {
        return timestamp
    }

}

class ChatRecordViewHolder(itemView: View): AbsViewHolder<ChatRecord>(itemView) {

    override fun bind(item: ChatRecord) {
        val textPanel: View = itemView.findViewById(R.id.text_panel)
        val textView: TextView = itemView.findViewById(R.id.chat_text)
        val textViewLp = textPanel?.layoutParams
        val pRecv: View = itemView.findViewById(R.id.portrait_receive)
        val pSend: View = itemView.findViewById(R.id.portrait_send)

        when (item.direction) {
            Direction.Send -> {
                pRecv?.visibility = View.INVISIBLE
                pSend?.visibility = View.VISIBLE

                if (textViewLp is FrameLayout.LayoutParams) {
                    textViewLp?.gravity = Gravity.END
                }

                textPanel?.setBackgroundResource(R.drawable.chat_right)
            }

            Direction.Receive -> {
                pRecv?.visibility = View.VISIBLE
                pSend?.visibility = View.INVISIBLE

                if (textViewLp is FrameLayout.LayoutParams) {
                    textViewLp?.gravity = Gravity.START
                }

                textPanel?.setBackgroundResource(R.drawable.chat_left)
            }
        }

        textView?.text = item.text
    }

}