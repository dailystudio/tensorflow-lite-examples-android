package com.dailystudio.tflite.example.image.superresolution.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.dailystudio.devbricksx.development.Logger
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.TFLiteModel
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SuperResolutionModel(
    context: Context,
    modelPath: String,
    device: Model.Device,
    numOfThreads: Int,
    useXNNPACK: Boolean
): TFLiteModel(context, modelPath, device, numOfThreads, useXNNPACK) {
    companion object {
        const val INPUT_IMAGE_SIZE = 50
        const val OUT_IMAGE_SIZE = 200
    }

    fun superResolution(bitmap: Bitmap): SuperRes? {
        Logger.debug("[RES] bitmap input: ${bitmap.width} x ${bitmap.height}")
        if (bitmap.width != INPUT_IMAGE_SIZE || bitmap.height != INPUT_IMAGE_SIZE) {
            return SuperRes(bitmap, bitmap)
        }

        val input = bitmapToByteBuffer(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE)
        val outputsForSuperRes = HashMap<Int, Any>()
        val outputImage =
            Array(1) { Array(OUT_IMAGE_SIZE) { Array(OUT_IMAGE_SIZE) { FloatArray(3) } } }
        outputsForSuperRes[0] = outputImage

        val inputsForSuperRes = arrayOf<Any>(input)
        getInterpreter()?.runForMultipleInputsOutputs(
            inputsForSuperRes,
            outputsForSuperRes
        )

        val superRes = Bitmap.createBitmap(
            convertArrayToBitmap(outputImage, OUT_IMAGE_SIZE, OUT_IMAGE_SIZE),
            OUT_IMAGE_SIZE,
            OUT_IMAGE_SIZE,
            Bitmap.Config.ARGB_8888)

        return SuperRes(bitmap, superRes)
    }


    fun bitmapToByteBuffer(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        mean: Float = 0.0f,
        std: Float = 255.0f
    ): FloatBuffer {
        val inputImage = FloatBuffer.allocate(1 * width * height * 3)
//        inputImage.order(ByteOrder.nativeOrder())
        inputImage.rewind()

        val intValues = IntArray(width * height)
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        var pixel = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = intValues[pixel++]

                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                inputImage.put(((value shr 16) and 0xFF).toFloat())
                inputImage.put(((value shr 8) and 0xFF).toFloat())
                inputImage.put((value and 0xFF).toFloat())
            }
        }

        inputImage.rewind()
        return inputImage
    }


    fun convertArrayToBitmap(imageArray: Array<Array<Array<FloatArray>>>,
                             imageWidth: Int,
                             imageHeight: Int
    ): IntArray {
        val pixels = imageWidth * imageHeight
        val data = IntArray(pixels)

        for (x in imageArray[0].indices) {
            for (y in imageArray[0][0].indices) {
                val color = Color.rgb(
                    (imageArray[0][x][y][0]).coerceIn(0f, 255f).toInt(),
                    (imageArray[0][x][y][1]).coerceIn(0f, 255f).toInt(),
                    (imageArray[0][x][y][2]).coerceIn(0f, 255f).toInt()
                )

                data[x * imageHeight + y] = color
            }
        }

        return data
    }
}