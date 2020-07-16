package com.dailystudio.tflite.example.image.styletransfer

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsCardViewHolder
import com.dailystudio.tflite.example.common.Constants
import com.nostra13.universalimageloader.core.ImageLoader

@ListFragment(gridLayout = true, columns = 3)
@Adapter(viewType = ViewType.Card, viewHolder = StyleImageViewHolder::class)
@ViewModel
@InMemoryRepository(key = Int::class)
@InMemoryManager(key = Int::class)
@DiffUtil
data class StyleImage(val id: Int,
                      val name: String,
                      val assetPath: String,
                      val selected: Boolean = false): InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

}

class StyleImageViewHolder(itemView: View) : AbsCardViewHolder<StyleImage>(itemView) {

    override fun bindMedia(item: StyleImage, imageView: ImageView?) {
        imageView?.let {
            ImageLoader.getInstance().displayImage(
                "assets://${item.assetPath}",
                it, Constants.DEFAULT_IMAGE_LOADER_OPTIONS_BUILDER.build())
        }
    }

    override fun bindTitle(item: StyleImage, titleView: TextView?) {
        titleView?.visibility = View.GONE
    }

    override fun getMedia(item: StyleImage): Drawable? {
        return null
    }

    override fun getTitle(item: StyleImage): CharSequence? {
        return null
    }

}