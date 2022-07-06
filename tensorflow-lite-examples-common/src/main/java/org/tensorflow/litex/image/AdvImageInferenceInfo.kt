package org.tensorflow.litex.image

import android.content.Context
import android.util.Size
import org.tensorflow.litex.InferenceInfoItem
import com.dailystudio.tflite.example.common.R


class AdvanceInferenceInfo(var frameSize: Size = Size(0, 0),
                           var preProcessTime: Long = 0,
                           var flattenTime: Long = 0) : ImageInferenceInfo() {

    override fun toInfoItems(context: Context): MutableList<InferenceInfoItem> {
        val items = super.toInfoItems(context)

        val idStart = items.size

        val resources = context.resources

        val itemFlattenTime = InferenceInfoItem(idStart+ 1, R.drawable.ic_info_flatten,
            resources.getString(R.string.label_info_flatten_time), "%d ms".format(flattenTime))
        items.add(itemFlattenTime)

        val itemPreProcessTime = InferenceInfoItem(idStart+ 2, R.drawable.ic_info_preprocess,
            resources.getString(R.string.label_info_pre_process_time), "%d ms".format(preProcessTime))
        items.add(itemPreProcessTime)

        return items
    }

}
