package org.tensorflow.lite.examples.imagesegmentation

import android.graphics.Bitmap
import android.graphics.Matrix

data class SegmentationResult(val maskBitmap: Bitmap,
                              val preScaledRevertTransform: Matrix? = null
)