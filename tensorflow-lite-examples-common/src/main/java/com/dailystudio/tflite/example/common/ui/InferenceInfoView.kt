package com.dailystudio.tflite.example.common.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.R

class InferenceInfoAdapter: ListAdapter<InferenceInfoItem, InferenceInfoItemViewHolder>(
    DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<InferenceInfoItem> = InferenceInfoItemDiffUtil()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InferenceInfoItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_inference_info_item, null)

        return InferenceInfoItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: InferenceInfoItemViewHolder, position: Int) {
        val item = getItem(position) ?: return

        holder.bind(item)
    }

}

class InferenceInfoView: FrameLayout {

    companion object {

        const val DEFAULT_COLUMNS = 3

    }

    private var itemsContainer: LinearLayout? = null
    private var infoItemViews: MutableMap<Int, InferenceInfoItemView> = mutableMapOf()

    private var lastItems: List<InferenceInfoItem>? = null

    private var columns = DEFAULT_COLUMNS

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
            R.layout.layout_inference_info_view, this)

        setupViews()
    }

    private fun setupViews() {
        itemsContainer = findViewById(R.id.items_container)
    }

    fun setColumns(cols: Int) {
        if (cols == columns) {
            return
        }

        columns = cols

        requestInvalidate(lastItems)
    }

    fun setInferenceInfo(info: InferenceInfo) {
        val items: List<InferenceInfoItem> = dumpInfoItems(info)

        requestInvalidate(items)

        lastItems = items
    }

    private fun requestInvalidate(items: List<InferenceInfoItem>?) {
        var layoutDirty = false
        if (itemsContainer?.childCount != items?.size) {
            layoutDirty = true
        } else {
            items?.let {
                for (item in it) {
                    if (!infoItemViews.containsKey(item.getKey())) {
                        layoutDirty = true
                        break
                    }
                }
            }

        }

        if (layoutDirty) {
            invalidateLayout(items)
        }

        invalidateContent(items)
    }

    private fun invalidateLayout(items: List<InferenceInfoItem>?) {
        itemsContainer?.let { container ->
            container.removeAllViews()
            infoItemViews.clear()

            if (items == null) {
                return
            }

            val layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )


            var itemView: InferenceInfoItemView
            var rowGroupView: LinearLayout? = null
            var lastRowGroupView: LinearLayout? = null

            for (i in items.indices) {
                if (i % columns == 0) {
                    rowGroupView = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }

                    container.addView(rowGroupView)

                    lastRowGroupView = rowGroupView
                }

                rowGroupView?.let {
                    itemView = InferenceInfoItemView(context)

                    it.addView(itemView, layoutParams)

                    infoItemViews.put(items[i].getKey(), itemView)
                }
            }

            lastRowGroupView?.let {
                val blankSpaces = columns - items.size % columns
                for (i in 0 until blankSpaces) {
                    it.addView(InferenceInfoItemView(context), layoutParams)
                }
            }
        }
    }

    private fun invalidateContent(items: List<InferenceInfoItem>?) {
        if (items == null) {
            return
        }

        for (item in items) {
            val itemView = infoItemViews[item.getKey()]
            itemView?.setItemInfo(item)
        }
    }

    protected open fun dumpInfoItems(info: InferenceInfo): MutableList<InferenceInfoItem> {
        val items = mutableListOf<InferenceInfoItem>()

        val itemImageSize = InferenceInfoItem(1, R.drawable.ic_info_image_size,
            resources.getString(R.string.label_info_image_size), info.imageSize.toString())
        items.add(itemImageSize)

        val itemImageRotation = InferenceInfoItem(2, R.drawable.ic_info_image_rotation,
            resources.getString(R.string.label_info_image_rotation), info.imageRotation.toString())
        items.add(itemImageRotation)

        val itemScreenRotation = InferenceInfoItem(3, R.drawable.ic_info_screen_rotation,
            resources.getString(R.string.label_info_screen_rotation), info.screenRotation.toString())
        items.add(itemScreenRotation)

        val itemInferenceImageSize = InferenceInfoItem(4, R.drawable.ic_info_inference_image_size,
            resources.getString(R.string.label_info_inference_image_size), info.inferenceImageSize.toString())
        items.add(itemInferenceImageSize)

        val itemInferenceTime = InferenceInfoItem(5, R.drawable.ic_info_inference_time,
            resources.getString(R.string.label_info_inference_time),
            "%d ms".format(info.inferenceTime))
        items.add(itemInferenceTime)

        return items
    }

}