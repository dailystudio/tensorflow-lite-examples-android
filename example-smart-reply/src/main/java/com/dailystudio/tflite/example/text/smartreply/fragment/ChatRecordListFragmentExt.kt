package com.dailystudio.tflite.example.text.smartreply.fragment

import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.tflite.example.text.smartreply.ChatRecord
import com.dailystudio.tflite.example.text.smartreply.ui.ChatRecordsAdapter
import kotlin.math.max

class ChatRecordListFragmentExt: ChatRecordsListFragment() {

//    override fun onCreateLayoutManager(): RecyclerView.LayoutManager {
//        val layoutManager = super.onCreateLayoutManager()
//
//        if (layoutManager is LinearLayoutManager) {
//            layoutManager.reverseLayout = true
//        }
//
//        return layoutManager
//    }

    override fun submitData(adapter: ChatRecordsAdapter, data: PagedList<ChatRecord>) {
        super.submitData(adapter, data)

        val recyclerView: RecyclerView? = view?.findViewById(android.R.id.list)

        recyclerView?.smoothScrollToPosition(max(0, data.size - 1))
    }
}