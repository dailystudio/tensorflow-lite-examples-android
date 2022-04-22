package com.dailystudio.tflite.example.image.styletransfer.fragment

import android.view.View
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.tflite.example.image.styletransfer.StyleImage
import com.dailystudio.tflite.example.image.styletransfer.StyleTransferPrefs
import com.dailystudio.tflite.example.image.styletransfer.ui.StyleImagesAdapter

class StyleImagesListFragmentExt : StyleImagesListFragment() {

    private var lastPosition: Int = -1

    override fun onItemClick(
        recyclerView: RecyclerView,
        itemView: View,
        position: Int,
        item: StyleImage,
        id: Long
    ) {
        super.onItemClick(recyclerView, itemView, position, item, id)

        selectStyle(position, lastPosition)
    }

    private fun selectStyle(newPos: Int,
                            oldPos: Int) {
        val list = adapter?.currentList

        var newItem: StyleImage? = null
        var oldItem: StyleImage? = null
        list?.let {
            newItem = it[newPos]
            oldItem = if (oldPos != -1) it[oldPos] else null

            newItem?.apply { selected = true }
            oldItem?.apply { selected = false }
        }

        adapter?.notifyItemChanged(newPos)
        adapter?.notifyItemChanged(oldPos)

        newItem?.let {
            notifySelection(it.name)
        }

        lastPosition = newPos
    }

    override fun submitData(adapter: StyleImagesAdapter, data: List<StyleImage>) {
        lastPosition = -1
        val selectedStyle = StyleTransferPrefs.getSelectedStyle(requireContext())

        for ((i, item) in data.withIndex()) {
            if (item.name == selectedStyle) {
                item.selected = true
                lastPosition = i
            } else {
                item.selected = false
            }
        }

        if (lastPosition == -1 && data.isNotEmpty()) {
            lastPosition = 0

            data[0]?.apply {
                selected = true
                notifySelection(name)
            }
        }

        super.submitData(adapter, data)
    }

    private fun notifySelection(style: String) {
        StyleTransferPrefs.setSelectedStyle(requireContext(), style)
    }

}