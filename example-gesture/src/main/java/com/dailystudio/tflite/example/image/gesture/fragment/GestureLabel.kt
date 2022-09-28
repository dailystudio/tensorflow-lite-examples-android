package com.dailystudio.tflite.example.image.gesture.fragment

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import com.dailystudio.devbricksx.annotations.data.InMemoryCompanion
import com.dailystudio.devbricksx.annotations.fragment.ListFragment
import com.dailystudio.devbricksx.annotations.view.Adapter
import com.dailystudio.devbricksx.annotations.view.ViewType
import com.dailystudio.devbricksx.annotations.viewmodel.ViewModel
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsSingleLineViewHolder
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.image.gesture.R

@ListFragment(layout = R.layout.fragment_gesture_labels_list,
    gridLayout = true,
    columns = 4)
@Adapter(viewHolder = GestureLabelViewHolder::class,
    viewType = ViewType.Customized,
    layout = R.layout.layout_gesture_label
)
@ViewModel
@InMemoryCompanion
data class GestureLabel(val id: Int,
                        val label: String,
                        val prop: Float = 0f,
                        var selected:  Boolean = false): InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

}

class GestureLabelViewHolder(itemView: View): AbsSingleLineViewHolder<GestureLabel>(itemView) {

    override fun bind(item: GestureLabel) {
        super.bind(item)

        val context = itemView.context

        val view: View = itemView.findViewById(R.id.gesture_label_panel)
        view?.let {
            val background = ResourcesCompatUtils.getDrawable(context,
                if (item.selected) {
                    R.drawable.gesture_label_bg_selected
                } else {
                    R.drawable.gesture_label_bg
                }
            )

            it.background = background
        }
    }

    override fun bindText(item: GestureLabel, titleView: TextView?) {
        super.bindText(item, titleView)

        val context = itemView.context

        titleView?.let {
            val color = ResourcesCompatUtils.getColor(context,
                if (item.selected) {
                    R.color.colorPrimary
                } else {
                    R.color.colorAccent
                }
            )

            it.setTextColor(color)
        }
    }

    override fun getIcon(item: GestureLabel): Drawable? {
        return null
    }

    override fun getText(item: GestureLabel): CharSequence? {
        return item.label.capitalize()
    }

}
