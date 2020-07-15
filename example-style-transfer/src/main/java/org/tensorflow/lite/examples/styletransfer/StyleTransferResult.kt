package org.tensorflow.lite.examples.styletransfer

import android.graphics.Bitmap
import android.graphics.Matrix

data class StyleTransferResult(val maskBitmap: Bitmap,
                               val items: Set<String>)