package org.tensorflow.lite.examples.imagesegmentation

import android.graphics.Bitmap
import android.graphics.Matrix

data class SegmentationResult(val maskBitmap: Bitmap,
                              val items: Set<String>)