/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package org.tensorflow.lite.examples.ocr

import android.graphics.Bitmap
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfRotatedRect

data class DetectionResult(
  val boundingBoxesMat: MatOfRotatedRect,
  val indicesMat: MatOfInt,
  val ratioWidth: Float,
  val ratioHeight: Float,
)

data class RecognitionResult(
  val bitmapResult: Bitmap,
  val executionLog: String,
  // A map between words and colors of the items found.
  val itemsFound: Map<String, Int>,
) {
  var detectionTime: Long = 0L
  var recognitionTime: Long = 0L

}
