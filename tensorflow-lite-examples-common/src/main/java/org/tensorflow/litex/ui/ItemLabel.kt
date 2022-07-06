package org.tensorflow.litex.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsViewHolder
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.R
import java.util.*

@ListFragment(layoutByName = "fragment_item_labels_list",
    gridLayout = true,
    columns = 3)
@Adapter(viewHolder = ItemLabelViewHolder::class,
    viewType = ViewType.Customized,
    layoutByName = "layout_item_label"
)
@ViewModel
@InMemoryRepository(key = Int::class)
@InMemoryManager(key = Int::class)
@DiffUtil
data class ItemLabel(val id: Int,
                     val name: String,
                     var label: String,
                     var color: Int = Color.TRANSPARENT
): InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

}

class ItemLabelViewHolder(itemView: View): AbsViewHolder<ItemLabel>(itemView) {

    override fun bind(item: ItemLabel) {
        val context = itemView.context

        val labelView: TextView? = itemView.findViewById(R.id.list_item_text_line_1st)
        labelView?.text = item.label.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.getDefault())
            } else {
                it.toString()
            }
        }

        val labelPanel: View? = itemView.findViewById(R.id.item_label_panel)
        labelPanel?.visibility = if (item.label.isBlank()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        if (item.color != Color.TRANSPARENT) {
            labelView?.setTextColor(item.color)
            labelPanel?.backgroundTintList = ColorStateList.valueOf(item.color)
        } else {
            labelView?.setTextColor(ResourcesCompatUtils.getColor(
                context, R.color.colorAccent))

            labelPanel?.backgroundTintList = null
        }
    }

}
