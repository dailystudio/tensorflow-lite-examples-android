package com.dailystudio.tflite.example.text.smartreply.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatRecordListFragmentExt: ChatRecordsListFragment() {

    override fun onCreateLayoutManager(): RecyclerView.LayoutManager {
        val layoutManager = super.onCreateLayoutManager()

        if (layoutManager is LinearLayoutManager) {
            layoutManager.reverseLayout = true
        }

        return layoutManager
    }
}