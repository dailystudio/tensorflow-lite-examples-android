package com.dailystudio.tflite.example.fragment

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.devbricksx.app.activity.ActivityLauncher
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.Example

class ExamplesFragmentExt : ExamplesListFragment() {

    override fun onItemClick(
        recyclerView: RecyclerView,
        itemView: View,
        position: Int,
        item: Example,
        id: Long) {
        super.onItemClick(recyclerView, itemView, position, item, id)

        val context = itemView.context

        Logger.debug("example selected: $item")

        ActivityLauncher.launchActivity(context, item.getBaseIntent(context))
    }

}