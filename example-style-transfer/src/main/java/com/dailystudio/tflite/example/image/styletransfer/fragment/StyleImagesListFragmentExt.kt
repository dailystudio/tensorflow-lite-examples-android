package com.dailystudio.tflite.example.image.styletransfer.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StyleImagesListFragmentExt : StyleImagesListFragment() {

    override fun onCreateLayoutManager(): RecyclerView.LayoutManager {
        val layoutManager = super.onCreateLayoutManager()

        if (layoutManager is LinearLayoutManager) {
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        }

        return layoutManager
    }

}