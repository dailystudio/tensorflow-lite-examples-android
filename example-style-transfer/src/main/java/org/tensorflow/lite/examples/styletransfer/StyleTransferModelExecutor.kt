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
import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import com.dailystudio.tflite.example.common.image.AdvanceInferenceInfo
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.collections.set
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.TFLiteModel


enum class FSTModel {
  FastStyleTransferInt8,
  FastStyleTransferFloat16
}

@SuppressWarnings("GoodTime")
class StyleTransferModelExecutor(
  context: Context,
  model: FSTModel,
  device: Model.Device,
  numOfThreads: Int,
  userXNNPack: Boolean
): TFLiteModel(context,
  if (model == FSTModel.FastStyleTransferFloat16) {
    arrayOf(STYLE_PREDICT_FLOAT16_MODEL, STYLE_TRANSFER_FLOAT16_MODEL)
  } else {
    arrayOf(STYLE_PREDICT_INT8_MODEL, STYLE_TRANSFER_INT8_MODEL)
  }
  , arrayOf(device, device), arrayOf(numOfThreads, numOfThreads),
  arrayOf(userXNNPack, userXNNPack)) {


  private val interpreterPredict: Interpreter?
    get() {
      return getInterpreter(0)
    }

  private val interpreterTransform: Interpreter?
    get() {
      return getInterpreter(1)
    }

  private var fullExecutionTime = 0L
  private var preProcessTime = 0L
  private var stylePredictTime = 0L
  private var styleTransferTime = 0L
  private var postProcessTime = 0L

  private var cachedOutputsForPredict: HashMap<Int, Any>? = null

  companion object {
    private const val TAG = "StyleTransferMExec"
    private const val STYLE_IMAGE_SIZE = 256
    private const val CONTENT_IMAGE_SIZE = 384
    private const val BOTTLENECK_SIZE = 100
    private const val STYLE_PREDICT_INT8_MODEL = "style_predict_quantized_256.tflite"
    private const val STYLE_TRANSFER_INT8_MODEL = "style_transfer_quantized_384.tflite"
    private const val STYLE_PREDICT_FLOAT16_MODEL = "style_predict_f16_256.tflite"
    private const val STYLE_TRANSFER_FLOAT16_MODEL = "style_transfer_f16_384.tflite"
  }

  fun fastExecute(contentBitmap: Bitmap,
                  styleBitmap: Bitmap,
                  info: AdvanceInferenceInfo,
                  skipPredict: Boolean = false
  ): Bitmap {
    try {
      Log.i(TAG, "running models: $skipPredict")

      fullExecutionTime = SystemClock.uptimeMillis()
      preProcessTime = SystemClock.uptimeMillis()

      val contentArray =
        ImageUtils.bitmapToByteBuffer(contentBitmap, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)
      val input = ImageUtils.bitmapToByteBuffer(styleBitmap, STYLE_IMAGE_SIZE, STYLE_IMAGE_SIZE)

      val inputsForPredict = arrayOf<Any>(input)
      var outputsForPredict = HashMap<Int, Any>()
      val styleBottleneck = Array(1) { Array(1) { Array(1) { FloatArray(BOTTLENECK_SIZE) } } }
      outputsForPredict[0] = styleBottleneck

      stylePredictTime = SystemClock.uptimeMillis()
      // The results of this inference could be reused given the style does not change
      // That would be a good practice in case this was applied to a video stream.
      if (!skipPredict) {
        interpreterPredict?.runForMultipleInputsOutputs(inputsForPredict, outputsForPredict)
        cachedOutputsForPredict = outputsForPredict
      } else {
        outputsForPredict = cachedOutputsForPredict ?: HashMap()
      }

      stylePredictTime = SystemClock.uptimeMillis() - stylePredictTime
      Log.d(TAG, "Style Predict Time to run: $stylePredictTime")
      info.preProcessTime = stylePredictTime

      val inputsForStyleTransfer = arrayOf(contentArray, outputsForPredict[0])
      val outputsForStyleTransfer = HashMap<Int, Any>()
      val outputImage =
        Array(1) { Array(CONTENT_IMAGE_SIZE) { Array(CONTENT_IMAGE_SIZE) { FloatArray(3) } } }
      outputsForStyleTransfer[0] = outputImage

      styleTransferTime = SystemClock.uptimeMillis()
      interpreterTransform?.runForMultipleInputsOutputs(
        inputsForStyleTransfer,
        outputsForStyleTransfer
      )
      styleTransferTime = SystemClock.uptimeMillis() - styleTransferTime
      Log.d(TAG, "Style apply Time to run: $styleTransferTime")
      info.inferenceTime = styleTransferTime

      postProcessTime = SystemClock.uptimeMillis()
      val pixels = convertArrayToInt(outputImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)
      val styledImage =
        com.dailystudio.devbricksx.utils.ImageUtils.intArrayToBitmap(pixels,
          CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)
//      val styledImage = ImageUtils.convertArrayToBitmap(outputImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)
      postProcessTime = SystemClock.uptimeMillis() - postProcessTime
      info.flattenTime = postProcessTime

      fullExecutionTime = SystemClock.uptimeMillis() - fullExecutionTime
      Log.d(TAG, "Time to run everything: $fullExecutionTime")

      return styledImage
    } catch (e: Exception) {
      val exceptionLog = "something went wrong: ${e.message}"
      Log.d(TAG, exceptionLog)

      val emptyBitmap =
        ImageUtils.createEmptyBitmap(
          CONTENT_IMAGE_SIZE,
          CONTENT_IMAGE_SIZE
        )
      return emptyBitmap
    }
  }

  private fun convertArrayToInt(imageArray: Array<Array<Array<FloatArray>>>,
                        imageWidth: Int,
                        imageHeight: Int
  ): IntArray {
    val pixels = imageWidth * imageHeight
    val data = IntArray(pixels)

    for (x in imageArray[0].indices) {
      for (y in imageArray[0][0].indices) {
        val color = Color.rgb(
          (imageArray[0][x][y][2] * 255).toInt(),
          (imageArray[0][x][y][1] * 255).toInt(),
          (imageArray[0][x][y][0] * 255).toInt()
        )

        data[x * imageHeight + y] = color
      }
    }

    return data
  }

  fun execute(
    contentImagePath: String,
    styleImageName: String,
    context: Context
  ): ModelExecutionResult {
    try {
      Log.i(TAG, "running models")

      fullExecutionTime = SystemClock.uptimeMillis()
      preProcessTime = SystemClock.uptimeMillis()

      val contentImage = ImageUtils.decodeBitmap(File(contentImagePath))
      val contentArray =
        ImageUtils.bitmapToByteBuffer(contentImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)
      val styleBitmap =
        ImageUtils.loadBitmapFromResources(context, "thumbnails/$styleImageName")
      val input = ImageUtils.bitmapToByteBuffer(styleBitmap, STYLE_IMAGE_SIZE, STYLE_IMAGE_SIZE)

      val inputsForPredict = arrayOf<Any>(input)
      val outputsForPredict = HashMap<Int, Any>()
      val styleBottleneck = Array(1) { Array(1) { Array(1) { FloatArray(BOTTLENECK_SIZE) } } }
      outputsForPredict[0] = styleBottleneck
      preProcessTime = SystemClock.uptimeMillis() - preProcessTime

      stylePredictTime = SystemClock.uptimeMillis()
      // The results of this inference could be reused given the style does not change
      // That would be a good practice in case this was applied to a video stream.
      interpreterPredict?.runForMultipleInputsOutputs(inputsForPredict, outputsForPredict)
      stylePredictTime = SystemClock.uptimeMillis() - stylePredictTime
      Log.d(TAG, "Style Predict Time to run: $stylePredictTime")

      val inputsForStyleTransfer = arrayOf(contentArray, styleBottleneck)
      val outputsForStyleTransfer = HashMap<Int, Any>()
      val outputImage =
        Array(1) { Array(CONTENT_IMAGE_SIZE) { Array(CONTENT_IMAGE_SIZE) { FloatArray(3) } } }
      outputsForStyleTransfer[0] = outputImage

      styleTransferTime = SystemClock.uptimeMillis()
      interpreterTransform?.runForMultipleInputsOutputs(
        inputsForStyleTransfer,
        outputsForStyleTransfer
      )
      styleTransferTime = SystemClock.uptimeMillis() - styleTransferTime
      Log.d(TAG, "Style apply Time to run: $styleTransferTime")

      postProcessTime = SystemClock.uptimeMillis()
      var styledImage =
        ImageUtils.convertArrayToBitmap(outputImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)
      postProcessTime = SystemClock.uptimeMillis() - postProcessTime

      fullExecutionTime = SystemClock.uptimeMillis() - fullExecutionTime
      Log.d(TAG, "Time to run everything: $fullExecutionTime")

      return ModelExecutionResult(
        styledImage,
        preProcessTime,
        stylePredictTime,
        styleTransferTime,
        postProcessTime,
        fullExecutionTime,
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
      return ModelExecutionResult(
        emptyBitmap, errorMessage = e.message!!
      )
    }
  }

  @Throws(IOException::class)
  private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelFile)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    fileDescriptor.close()
    return retFile
  }

  private fun formatExecutionLog(): String {
    val sb = StringBuilder()
    sb.append("Input Image Size: $CONTENT_IMAGE_SIZE x $CONTENT_IMAGE_SIZE\n")
    sb.append("GPU enabled: ${devices[0] == Model.Device.GPU}\n")
    sb.append("Number of threads: $\n")
    sb.append("Pre-process execution time: $preProcessTime ms\n")
    sb.append("Predicting style execution time: $stylePredictTime ms\n")
    sb.append("Transferring style execution time: $styleTransferTime ms\n")
    sb.append("Post-process execution time: $postProcessTime ms\n")
    sb.append("Full execution time: $fullExecutionTime ms\n")
    return sb.toString()
  }

}
