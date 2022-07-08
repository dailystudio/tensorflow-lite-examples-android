package com.dailystudio.tflite.example.image.ocr.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.dailystudio.devbricksx.development.Logger
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.examples.ocr.DetectionResult
import org.tensorflow.lite.examples.ocr.ImageUtils
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.AssetFileLiteModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OCRRecognitionModel(
    context: Context,
    numOfThreads: Int,
): AssetFileLiteModel(context, RECOGNITION_TF_MODEL_PATH,
    Model.Device.CPU, numOfThreads, false) {

    companion object {
        const val RECOGNITION_TF_MODEL_PATH = "text_recognition.tflite"

        private const val alphabets = "0123456789abcdefghijklmnopqrstuvwxyz"

        private const val recognitionImageHeight = 31
        private const val recognitionImageWidth = 200
        private const val recognitionImageMean = 0.toFloat()
        private const val recognitionImageStd = 255.toFloat()
        private const val recognitionModelOutputSize = 48
    }

    private val recognitionResult: ByteBuffer = ByteBuffer.allocateDirect(recognitionModelOutputSize * 8)

    override fun open() {
        super.open()
        try {
            if (!OpenCVLoader.initDebug()) {
                throw Exception("Unable to load OpenCV")
            } else {
                Logger.info("OpenCV loaded")
            }
        } catch (e: Exception) {
            val exceptionLog = "something went wrong: ${e.message}"
            Logger.error(exceptionLog)
        }

        recognitionResult.order(ByteOrder.nativeOrder())
    }

    fun recognizeTexts(
        data: Bitmap,
        detectionResult: DetectionResult,
        ocrResults: HashMap<String, Int>
    ): Bitmap {
        val bitmapWithBoundingBoxes = data.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmapWithBoundingBoxes)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10.toFloat()
        paint.setColor(Color.GREEN)

        val ratioWidth = detectionResult.ratioWidth
        val ratioHeight = detectionResult.ratioHeight

        for (i in detectionResult.indicesMat.toArray()) {
            val boundingBox = detectionResult.boundingBoxesMat.toArray()[i]
            val targetVertices = ArrayList<Point>()
            targetVertices.add(Point(0.toDouble(), (recognitionImageHeight - 1).toDouble()))
            targetVertices.add(Point(0.toDouble(), 0.toDouble()))
            targetVertices.add(Point((recognitionImageWidth - 1).toDouble(), 0.toDouble()))
            targetVertices.add(
                Point((recognitionImageWidth - 1).toDouble(), (recognitionImageHeight - 1).toDouble())
            )

            val srcVertices = ArrayList<Point>()

            val boundingBoxPointsMat = Mat()
            Imgproc.boxPoints(boundingBox, boundingBoxPointsMat)
            for (j in 0 until 4) {
                srcVertices.add(
                    Point(
                        boundingBoxPointsMat.get(j, 0)[0] * ratioWidth,
                        boundingBoxPointsMat.get(j, 1)[0] * ratioHeight
                    )
                )
                if (j != 0) {
                    canvas.drawLine(
                        (boundingBoxPointsMat.get(j, 0)[0] * ratioWidth).toFloat(),
                        (boundingBoxPointsMat.get(j, 1)[0] * ratioHeight).toFloat(),
                        (boundingBoxPointsMat.get(j - 1, 0)[0] * ratioWidth).toFloat(),
                        (boundingBoxPointsMat.get(j - 1, 1)[0] * ratioHeight).toFloat(),
                        paint
                    )
                }
            }
            canvas.drawLine(
                (boundingBoxPointsMat.get(0, 0)[0] * ratioWidth).toFloat(),
                (boundingBoxPointsMat.get(0, 1)[0] * ratioHeight).toFloat(),
                (boundingBoxPointsMat.get(3, 0)[0] * ratioWidth).toFloat(),
                (boundingBoxPointsMat.get(3, 1)[0] * ratioHeight).toFloat(),
                paint
            )

            val srcVerticesMat =
                MatOfPoint2f(srcVertices[0], srcVertices[1], srcVertices[2], srcVertices[3])
            val targetVerticesMat =
                MatOfPoint2f(targetVertices[0], targetVertices[1], targetVertices[2], targetVertices[3])
            val rotationMatrix = Imgproc.getPerspectiveTransform(srcVerticesMat, targetVerticesMat)
            val recognitionBitmapMat = Mat()
            val srcBitmapMat = Mat()
            Utils.bitmapToMat(data, srcBitmapMat)
            Imgproc.warpPerspective(
                srcBitmapMat,
                recognitionBitmapMat,
                rotationMatrix,
                Size(recognitionImageWidth.toDouble(), recognitionImageHeight.toDouble())
            )

            val recognitionBitmap =
                ImageUtils.createEmptyBitmap(
                    recognitionImageWidth,
                    recognitionImageHeight,
                    0,
                    Bitmap.Config.ARGB_8888
                )
            Utils.matToBitmap(recognitionBitmapMat, recognitionBitmap)

            val recognitionTensorImage =
                ImageUtils.bitmapToTensorImageForRecognition(
                    recognitionBitmap,
                    recognitionImageWidth,
                    recognitionImageHeight,
                    recognitionImageMean,
                    recognitionImageStd
                )

            recognitionResult.rewind()
            interpreter?.run(recognitionTensorImage.buffer, recognitionResult)

            var recognizedText = ""
            for (k in 0 until recognitionModelOutputSize) {
                var alphabetIndex = recognitionResult.getInt(k * 8)
                if (alphabetIndex in 0..alphabets.length - 1)
                    recognizedText = recognizedText + alphabets[alphabetIndex]
            }
            Log.d("Recognition result:", recognizedText)
            if (recognizedText != "") {
                ocrResults.put(recognizedText, getRandomColor())
            }
        }

        return bitmapWithBoundingBoxes
    }

    fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(
            (128),
            (255 * random.nextFloat()).toInt(),
            (255 * random.nextFloat()).toInt(),
            (255 * random.nextFloat()).toInt()
        )
    }
}