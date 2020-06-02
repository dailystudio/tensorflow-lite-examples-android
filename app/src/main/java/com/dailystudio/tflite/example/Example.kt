package com.dailystudio.tflite.example

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsInformativeCardViewHolder
import com.dailystudio.tflite.example.common.Constants
import com.nostra13.universalimageloader.core.ImageLoader

@ViewModel
@Adapter(viewHolder = ExampleViewHolder::class,
    viewType = ViewType.CardInformative
)
@ListFragment(gridLayout = false)
@DiffUtil
@InMemoryRepository(key = Int::class)
@InMemoryManager(key = Int::class)
class Example(val id: Int,
              val `package`: String,
              val title: String,
              val description: String,
              val image: String) : InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

    override fun toString(): String {
        return buildString {
            append("id: $id, ")
            append("package: $`package`, ")
            append("title: $title, ")
            append("description: $description, ")
            append("image: $image")
        }
    }

    fun getBaseIntent(context: Context): Intent {
        return Intent().apply {
            this@Example.`package`?.let {
                action = Constants.ACTION_MAIN
                addCategory(Intent.CATEGORY_DEFAULT)

                setClassName(it,
                    buildString {
                        append(it)
                        append(Constants.EXAMPLE_ACTIVITY_CLASS_NAME)
                    }
                )
            }
        }
    }

}

class ExampleViewHolder(itemView: View): AbsInformativeCardViewHolder<Example>(itemView) {

    override fun bindMedia(item: Example, iconView: ImageView?) {
        ImageLoader.getInstance().displayImage(
            item.image, iconView, Constants.DEFAULT_IMAGE_LOADER_OPTIONS)
    }

    override fun getMedia(item: Example): Drawable? {
        return null
    }

    override fun getTitle(item: Example): CharSequence? {
        return item.title
    }

    override fun getSupportingText(item: Example): CharSequence? {
         return item.description
    }

    override fun shouldDisplayDivider(): Boolean {
        return true
    }

}