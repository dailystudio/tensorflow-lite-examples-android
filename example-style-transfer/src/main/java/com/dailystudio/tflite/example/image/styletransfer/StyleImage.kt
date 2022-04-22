package com.dailystudio.tflite.example.image.styletransfer

import android.view.View
import android.widget.ImageView
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsViewHolder
import com.dailystudio.tflite.example.common.Constants
import com.nostra13.universalimageloader.core.ImageLoader

@ListFragment(gridLayout = true, columns = 3)
@Adapter(viewType = ViewType.Customized,
    viewHolder = StyleImageViewHolder::class,
    layout = R.layout.style_card_view)
@ViewModel
@InMemoryRepository(key = Int::class)
@InMemoryManager(key = Int::class)
@DiffUtil
data class StyleImage(val id: Int,
                      val name: String,
                      val assetPath: String,
                      var selected: Boolean = false): InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is StyleImage) {
            false
        } else {
            (id == other.id && selected == other.selected)
        }
    }

}

class StyleImageViewHolder(itemView: View) : AbsViewHolder<StyleImage>(itemView) {

    override fun bind(item: StyleImage) {
        val imageView: ImageView? = itemView.findViewById(R.id.style_image)
        imageView?.let {
            val builder = Constants.DEFAULT_IMAGE_LOADER_OPTIONS_BUILDER

            ImageLoader.getInstance().displayImage(
                "assets://${item.assetPath}",
                it, builder.build()
            )
        }

        val selectionView: View? = itemView.findViewById(R.id.style_selection)
        selectionView?.visibility = if (item.selected) View.VISIBLE else View.INVISIBLE
    }

}