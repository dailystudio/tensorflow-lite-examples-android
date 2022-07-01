/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.styletransfer

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import kotlin.collections.set
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.AssetFileLiteModel

enum class FSTModel {
  FastStyleTransferInt8,
  FastStyleTransferFloat16
}

class PredictModelExecutor(
  context: Context,
  modelPath: String,
  device: Model.Device,
  numOfThreads: Int,
  userXNNPack: Boolean
): AssetFileLiteModel(context, modelPath, device, numOfThreads, userXNNPack) {

  companion object {
    private const val TAG = "PredictMExec"

    private const val STYLE_IMAGE_SIZE = 256
    private const val BOTTLENECK_SIZE = 100

    const val STYLE_PREDICT_INT8_MODEL = "style_predict_quantized_256.tflite"
    const val STYLE_PREDICT_FLOAT16_MODEL = "style_predict_f16_256.tflite"

    val EMPTY_BOTTLENECK = createEmptyBottleneck(BOTTLENECK_SIZE)

    private fun createEmptyBottleneck(size: Int): StyleBottleneck {
      return Array(1) { Array(1) { Array(1) { FloatArray(size) } } }
    }
  }

  private var preProcessTime = 0L
  private var stylePredictTime = 0L

  fun execute(
    styleBitmap: Bitmap,
  ): PredictModelExecutionResult {
    preProcessTime = SystemClock.uptimeMillis()
    val styleBottleneck = createEmptyBottleneck(BOTTLENECK_SIZE)

    try {
      Log.i(TAG, "running models")

      val input = ImageUtils.bitmapToByteBuffer(styleBitmap,
        STYLE_IMAGE_SIZE,
        STYLE_IMAGE_SIZE
      )

      val inputsForPredict = arrayOf<Any>(input)
      val outputsForPredict = HashMap<Int, Any>()
      outputsForPredict[0] = styleBottleneck
      preProcessTime = SystemClock.uptimeMillis() - preProcessTime

      stylePredictTime = SystemClock.uptimeMillis()
      // The results of this inference could be reused given the style does not change
      // That would be a good practice in case this was applied to a video stream.
      interpreter?.runForMultipleInputsOutputs(inputsForPredict, outputsForPredict)
      stylePredictTime = SystemClock.uptimeMillis() - stylePredictTime
    } catch (e: Exception) {
      val exceptionLog = "something went wrong: ${e.message}"
      Log.d(TAG, exceptionLog)
    }

    return PredictModelExecutionResult(
      styleBottleneck,
      preProcessTime,
      stylePredictTime,
      formatExecutionLog()
    )
  }


  private fun formatExecutionLog(): String {
    val sb = StringBuilder()
    sb.append("Input Image Size: $STYLE_IMAGE_SIZE x $STYLE_IMAGE_SIZE\n")
    sb.append("GPU enabled: ${device == Model.Device.GPU}\n")
    sb.append("Number of threads: $numOfThreads\n")
    sb.append("Pre-process execution time: $preProcessTime ms\n")
    sb.append("Predicting style execution time: $stylePredictTime ms\n")
    return sb.toString()
  }

}
@SuppressWarnings("GoodTime")
class TransformModelExecutor(
  context: Context,
  modelPath: String,
  device: Model.Device,
  numOfThreads: Int,
  useXNNPack: Boolean
): AssetFileLiteModel(context,
  modelPath, device, numOfThreads, useXNNPack) {

  companion object {
    private const val TAG = "TransferMExec"

    private const val CONTENT_IMAGE_SIZE = 384

    const val STYLE_TRANSFER_INT8_MODEL = "style_transfer_quantized_384.tflite"
    const val STYLE_TRANSFER_FLOAT16_MODEL = "style_transfer_f16_384.tflite"

  }

  private var styleTransferTime = 0L
  private var postProcessTime = 0L

  fun execute(
    contentImage: Bitmap,
    styleBottleneck: StyleBottleneck,
  ): TransformModelExecutionResult {
    try {
      Log.i(TAG, "running models")

      val contentArray =
        ImageUtils.bitmapToByteBuffer(contentImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)

      styleTransferTime = SystemClock.uptimeMillis()
      val inputsForStyleTransfer = arrayOf(contentArray, styleBottleneck)
      val outputsForStyleTransfer = HashMap<Int, Any>()
      val outputImage =
        Array(1) { Array(CONTENT_IMAGE_SIZE) { Array(CONTENT_IMAGE_SIZE) { FloatArray(3) } } }
      outputsForStyleTransfer[0] = outputImage

      interpreter?.runForMultipleInputsOutputs(
        inputsForStyleTransfer,
        outputsForStyleTransfer
      )

      styleTransferTime = SystemClock.uptimeMillis() - styleTransferTime

      postProcessTime = SystemClock.uptimeMillis()
      val styledImage =
        ImageUtils.convertArrayToBitmap(outputImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)
      postProcessTime = SystemClock.uptimeMillis() - postProcessTime

      return TransformModelExecutionResult(
        styledImage,
        styleTransferTime,
        postProcessTime,
        formatExecutionLog()
      )
    } catch (e: Exception) {
      val exceptionLog = "something went wrong: ${e.message}"
      Log.d(TAG, exceptionLog)

      val emptyBitmap =
        ImageUtils.createEmptyBitmap(
          CONTENT_IMAGE_SIZE,
          CONTENT_IMAGE_SIZE
        )
      return TransformModelExecutionResult(
        emptyBitmap, errorMessage = e.message!!
      )
    }
  }

  private fun formatExecutionLog(): String {
    val sb = StringBuilder()
    sb.append("Input Image Size: $CONTENT_IMAGE_SIZE x $CONTENT_IMAGE_SIZE\n")
    sb.append("GPU enabled: ${device == Model.Device.GPU}\n")
    sb.append("Number of threads: $numOfThreads\n")
    sb.append("Transferring style execution time: $styleTransferTime ms\n")
    sb.append("Post-process execution time: $postProcessTime ms\n")
    return sb.toString()
  }

}
