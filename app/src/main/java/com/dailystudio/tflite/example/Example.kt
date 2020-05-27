package com.dailystudio.tflite.example

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsCardViewHolder
import com.dailystudio.devbricksx.utils.ColorUtils
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.*

@ViewModel
@Adapter(viewHolder = ExampleViewHolder::class,
    viewType = ViewType.Card)
@ListFragment(gridLayout = true)
@DiffUtil
@InMemoryRepository(key = Int::class)
@InMemoryManager(key = Int::class)
class Example(val id: Int,
              val title: String,
              val description: String,
              val image: String) : InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

    override fun toString(): String {
        return buildString {
            append("id: $id, ")
            append("title: $title, ")
            append("description: $description, ")
            append("image: $image")
        }
    }

}


class ExampleViewHolder(itemView: View): AbsCardViewHolder<Example>(itemView) {

    companion object {
        private val RAND : Random = Random(System.currentTimeMillis())
    }

    override fun bindMedia(item: Example, iconView: ImageView?) {
        ImageLoader.getInstance().displayImage(item.image, iconView)
    }

    override fun getMedia(item: Example): Drawable? {

        val color = Color.argb(255,
            RAND.nextInt(255),
            RAND.nextInt(255),
            RAND.nextInt(255))

        return ColorUtils.getColorDrawable(itemView.context, color)
    }

    override fun getTitle(item: Example): CharSequence? {
        return item.title
    }

    override fun getSupportingText(item: Example): CharSequence? {
         return null
    }

}