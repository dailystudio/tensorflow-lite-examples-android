package com.dailystudio.tflite.example.image.superresolution.model

import android.graphics.Bitmap

class SuperRes(val originalBitmap: Bitmap?,
               val superBitmap: Bitmap?) {

    override fun toString(): String {
        return buildString {
            append("original: ${originalBitmap?.width ?: 0} x ${originalBitmap?.height ?: 0}")
            append("superRes: ${superBitmap?.width ?: 0} x ${superBitmap?.height ?: 0}")
        }
    }
}