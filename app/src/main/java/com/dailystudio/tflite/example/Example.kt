package com.dailystudio.tflite.example

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsInformativeCardViewHolder
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.Constants
import com.dailystudio.tflite.example.common.Constants.DEFAULT_IMAGE_LOADER_OPTIONS_BUILDER
import com.nostra13.universalimageloader.core.ImageLoader


@ViewModel
@Adapter(viewHolder = ExampleViewHolder::class,
    viewType = ViewType.CardInformative,
    paged = true
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

    var installed: Boolean = false

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

    override fun equals(other: Any?): Boolean {
        if (!(other is Example)) {
            return false
        } else {
            return id == other.id
                    && `package` == other.`package`
                    && title == other.title
                    && description == other.description
                    && image == other.image
                    && installed == other.installed

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
        val builder = DEFAULT_IMAGE_LOADER_OPTIONS_BUILDER
        val context = itemView.context

        if (!item.installed) {
            builder.postProcessor { bitmap ->
                ImageUtils.tintBitmap(bitmap,
                    ResourcesCompatUtils.getColor(context, R.color.light_gray)
                )
            }
        } else {
            builder.postProcessor(null)
        }

        ImageLoader.getInstance().displayImage(
            item.image, iconView, builder.build())
    }

    override fun bindTitle(item: Example, titleView: TextView?) {
        super.bindTitle(item, titleView)

        adaptTextColor(item, titleView)
    }

    override fun bindSupportingText(item: Example, supportingTextView: TextView?) {
        super.bindSupportingText(item, supportingTextView)

        adaptTextColor(item, supportingTextView)
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

    private fun adaptTextColor(item: Example, textView: TextView?) {

        val context = itemView.context

        val titleColor = if (item.installed) {
            ResourcesCompatUtils.getColor(context, R.color.colorAccent)
        } else {
            ResourcesCompatUtils.getColor(context, R.color.light_gray)
        }

        textView?.setTextColor(titleColor)
    }

}