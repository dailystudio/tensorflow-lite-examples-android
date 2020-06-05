package com.dailystudio.tflite.example.common.utils

import android.graphics.Bitmap
import com.dailystudio.devbricksx.utils.ImageUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun ImageUtils.scaleBitmapWithRatio(bitmap: Bitmap?,
                                    destWidth: Int,
                                    destHeight: Int,
                                    reduceLoss: Boolean = true): Bitmap? {
    if (bitmap == null) {
        return bitmap
    }

    val srcInPortrait = (bitmap.width <= bitmap.height)
    val destInPortrait = (destWidth <= destHeight)

    val dw = if (srcInPortrait == destInPortrait) {
        destWidth
    } else {
        destHeight
    }.toFloat()

    val dh = if (srcInPortrait == destInPortrait) {
        destHeight
    } else {
        destWidth
    }.toFloat()

    val shrink = if (dw > dh) {
        dh < bitmap.height
    } else {
        dw < bitmap.width
    }

    val wRatio = dw / bitmap.width
    val hRatio = dh / bitmap.height

    val ratio = if (reduceLoss) {
        if (shrink) {
            max(wRatio, hRatio)
        } else {
            min(wRatio, hRatio)
        }
    } else {
        if (shrink) {
            min(wRatio, hRatio)
        } else {
            max(wRatio, hRatio)
        }
    }

    return scaleBitmap(bitmap,
        (bitmap.width * ratio).roundToInt(),
        (bitmap.height * ratio).roundToInt())
}