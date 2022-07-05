package org.tensorflow.lite.examples.digitclassifier

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import com.dailystudio.devbricksx.development.Logger
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.AssetFileLiteModel
import org.tensorflow.litex.TFLiteModel
import java.lang.Exception

class DigitClassifier(context: Context,
                      device: Model.Device,
                      numOfThreads: Int,
                      useXNNPACK: Boolean = true
) : AssetFileLiteModel(context, MODEL_FILE, device, numOfThreads, useXNNPACK){
  var isInitialized = false
    private set

  /** Executor to run inference task in the background */
  private val executorService: ExecutorService = Executors.newCachedThreadPool()

  private var inputImageWidth: Int = 0 // will be inferred from TF Lite model
  private var inputImageHeight: Int = 0 // will be inferred from TF Lite model
  private var modelInputSize: Int = 0 // will be inferred from TF Lite model

  override fun open() {
    super.open()

    interpreter?.let {
      // Read input shape from model file
      val inputShape = it.getInputTensor(0).shape()
      inputImageWidth = inputShape[1]
      inputImageHeight = inputShape[2]
      modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE

      Logger.debug("inputImageWidth = $inputImageWidth, inputImageHeight = $inputImageHeight")


      // Finish interpreter initialization
      isInitialized = true
      Log.d(TAG, "Initialized TFLite interpreter.")
    }
  }

  fun classify(bitmap: Bitmap): Pair<Int, Float> {
    if (!isInitialized) {
      throw IllegalStateException("TF Lite Interpreter is not initialized yet.")
    }

    var startTime: Long
    var elapsedTime: Long

    // Preprocessing: resize the input
    startTime = System.nanoTime()
    val resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
    val byteBuffer = convertBitmapToByteBuffer(resizedImage)
    elapsedTime = (System.nanoTime() - startTime) / 1000000
    Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")

    startTime = System.nanoTime()
    val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
    interpreter?.run(byteBuffer, result)
    elapsedTime = (System.nanoTime() - startTime) / 1000000
    Log.d(TAG, "Inference time = " + elapsedTime + "ms")

//    return getOutputString(result[0])
    return getOutput(result[0])
  }

  override fun close() {
    super.close()
    Log.d(TAG, "Closed TFLite interpreter.")
  }

  fun getInferenceSize(): Size {
    return Size(inputImageWidth, inputImageHeight)
  }

  private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
    byteBuffer.order(ByteOrder.nativeOrder())

    val pixels = IntArray(inputImageWidth * inputImageHeight)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    for (pixelValue in pixels) {
      val r = (pixelValue shr 16 and 0xFF)
      val g = (pixelValue shr 8 and 0xFF)
      val b = (pixelValue and 0xFF)

      // Convert RGB to grayscale and normalize pixel value to [0..1]
      val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
      byteBuffer.putFloat(normalizedPixelValue)
    }

    return byteBuffer
  }

  private fun getOutput(output: FloatArray): Pair<Int, Float> {
    val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1

    return Pair(maxIndex, output[maxIndex])
  }

  private fun getOutputString(output: FloatArray): String {
    val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
    return "Prediction Result: %d\nConfidence: %2f".format(maxIndex, output[maxIndex])
  }

  companion object {
    private const val TAG = "DigitClassifier"

    private const val MODEL_FILE = "mnist.tflite"

    private const val FLOAT_TYPE_SIZE = 4
    private const val PIXEL_SIZE = 1

    private const val OUTPUT_CLASSES_COUNT = 10
  }
}
