package org.tensorflow.lite.examples.imagesegmentation

import android.graphics.Bitmap

data class SegmentationResult(val maskBitmap: Bitmap,
                              val items: Set<String>)