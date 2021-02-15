package com.dailystudio.tflite.example.common.text

import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.tflite.example.common.text.fragment.ChatRecordsListFragment
import com.dailystudio.tflite.example.common.text.ui.ChatRecordsAdapter
import kotlin.math.max

class ChatRecordListFragmentExt: ChatRecordsListFragment() {

    override fun submitData(adapter: ChatRecordsAdapter, data: List<ChatRecord>) {
        super.submitData(adapter, data)

        val recyclerView: RecyclerView? = view?.findViewById(android.R.id.list)

        recyclerView?.smoothScrollToPosition(max(0, data.size - 1))
    }

}