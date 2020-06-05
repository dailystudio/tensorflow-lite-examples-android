package com.dailystudio.tflite.example.common.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.dailystudio.devbricksx.annotations.DiffUtil
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsViewHolder
import com.dailystudio.tflite.example.common.R


@DiffUtil
class InferenceInfoItem(val id: Int,
                        val iconResId: Int,
                        val label: CharSequence,
                        val value: CharSequence) : InMemoryObject<Int> {

    override fun getKey(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (other !is InferenceInfoItem) {
            return false
        }

        return (id == other.id)
                && (iconResId == other.iconResId)
                && (label == other.label)
                && (value == other.value)
    }

}

class InferenceInfoItemViewHolder(itemView: View) : AbsViewHolder<InferenceInfoItem>(itemView) {

    override fun bind(item: InferenceInfoItem) {
        val iconView: ImageView? = itemView.findViewById(R.id.info_item_icon)
        iconView?.setImageResource(item.iconResId)

        val labelView: TextView? = itemView.findViewById(R.id.info_item_label)
        labelView?.text = item.label

        val valueView: TextView? = itemView.findViewById(R.id.info_item_value)
        valueView?.text = item.value
    }

}

class InferenceInfoItemView: FrameLayout {

    private var iconView: ImageView? = null
    private var labelView: TextView? = null
    private var valueView: TextView? = null

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(
            R.layout.layout_inference_info_item, this)

        setupViews()
    }

    private fun setupViews() {
        iconView = findViewById(R.id.info_item_icon)
        labelView = findViewById(R.id.info_item_label)
        valueView = findViewById(R.id.info_item_value)
    }

    fun setItemInfo(item: InferenceInfoItem) {
        iconView?.setImageResource(item.iconResId)
        labelView?.text = item.label
        valueView?.text = item.value
    }

}