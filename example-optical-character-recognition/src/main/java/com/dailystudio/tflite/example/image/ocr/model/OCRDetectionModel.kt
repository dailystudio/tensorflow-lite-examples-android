package com.dailystudio.tflite.example.image.ocr.model

import android.content.Context
import android.graphics.Bitmap
import com.dailystudio.devbricksx.development.Logger
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.utils.Converters
import org.tensorflow.lite.examples.ocr.DetectionResult
import org.tensorflow.lite.examples.ocr.ImageUtils
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.AssetFileLiteModel
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OCRDetectionModel(
    context: Context,
    device: Model.Device,
    numOfThreads: Int,
    useXNNPack: Boolean
): AssetFileLiteModel(context, DETECTION_TF_MODEL_PATH, device, numOfThreads, useXNNPack) {

    companion object {
        const val DETECTION_TF_MODEL_PATH = "text_detection.tflite"

        private const val detectionImageHeight = 320
        private const val detectionImageWidth = 320
        private val detectionImageMeans =
            floatArrayOf(103.94.toFloat(), 116.78.toFloat(), 123.68.toFloat())
        private val detectionImageStds = floatArrayOf(1.toFloat(), 1.toFloat(), 1.toFloat())
        private const val detectionOutputNumRows = 80
        private const val detectionOutputNumCols = 80
        private const val detectionConfidenceThreshold = 0.5
        private const val detectionNMSThreshold = 0.4
    }

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
   }

    fun detectTexts(data: Bitmap): DetectionResult? {
        val ratioWidth = data.width.toFloat() / detectionImageWidth
        val ratioHeight = data.height.toFloat() / detectionImageHeight

        val indicesMat = MatOfInt()

        val detectionTensorImage =
            ImageUtils.bitmapToTensorImageForDetection(
                data,
                detectionImageWidth,
                detectionImageHeight,
                detectionImageMeans,
                detectionImageStds
            )

        val detectionInputs = arrayOf(detectionTensorImage.buffer.rewind())
        val detectionOutputs: HashMap<Int, Any> = HashMap<Int, Any>()

        val detectionScores =
            Array(1) { Array(detectionOutputNumRows) { Array(detectionOutputNumCols) { FloatArray(1) } } }
        val detectionGeometries =
            Array(1) { Array(detectionOutputNumRows) { Array(detectionOutputNumCols) { FloatArray(5) } } }
        detectionOutputs.put(0, detectionScores)
        detectionOutputs.put(1, detectionGeometries)

        interpreter?.runForMultipleInputsOutputs(detectionInputs, detectionOutputs)

        val transposeddetectionScores =
            Array(1) { Array(1) { Array(detectionOutputNumRows) { FloatArray(detectionOutputNumCols) } } }
        val transposedDetectionGeometries =
            Array(1) { Array(5) { Array(detectionOutputNumRows) { FloatArray(detectionOutputNumCols) } } }

        // transpose detection output tensors
        for (i in 0 until transposeddetectionScores[0][0].size) {
            for (j in 0 until transposeddetectionScores[0][0][0].size) {
                for (k in 0 until 1) {
                    transposeddetectionScores[0][k][i][j] = detectionScores[0][i][j][k]
                }
                for (k in 0 until 5) {
                    transposedDetectionGeometries[0][k][i][j] = detectionGeometries[0][i][j][k]
                }
            }
        }

        val detectedRotatedRects = ArrayList<RotatedRect>()
        val detectedConfidences = ArrayList<Float>()

        for (y in 0 until transposeddetectionScores[0][0].size) {
            val detectionScoreData = transposeddetectionScores[0][0][y]
            val detectionGeometryX0Data = transposedDetectionGeometries[0][0][y]
            val detectionGeometryX1Data = transposedDetectionGeometries[0][1][y]
            val detectionGeometryX2Data = transposedDetectionGeometries[0][2][y]
            val detectionGeometryX3Data = transposedDetectionGeometries[0][3][y]
            val detectionRotationAngleData = transposedDetectionGeometries[0][4][y]

            for (x in 0 until transposeddetectionScores[0][0][0].size) {
                if (detectionScoreData[x] < 0.5) {
                    continue
                }

                // Compute the rotated bounding boxes and confiences (heavily based on OpenCV example):
                // https://github.com/opencv/opencv/blob/master/samples/dnn/text_detection.py
                val offsetX = x * 4.0
                val offsetY = y * 4.0

                val h = detectionGeometryX0Data[x] + detectionGeometryX2Data[x]
                val w = detectionGeometryX1Data[x] + detectionGeometryX3Data[x]

                val angle = detectionRotationAngleData[x]
                val cos = Math.cos(angle.toDouble())
                val sin = Math.sin(angle.toDouble())

                val offset =
                    Point(
                        offsetX + cos * detectionGeometryX1Data[x] + sin * detectionGeometryX2Data[x],
                        offsetY - sin * detectionGeometryX1Data[x] + cos * detectionGeometryX2Data[x]
                    )
                val p1 = Point(-sin * h + offset.x, -cos * h + offset.y)
                val p3 = Point(-cos * w + offset.x, sin * w + offset.y)
                val center = Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y))

                val textDetection =
                    RotatedRect(
                        center,
                        Size(w.toDouble(), h.toDouble()),
                        (-1 * angle * 180.0 / Math.PI)
                    )
                detectedRotatedRects.add(textDetection)
                detectedConfidences.add(detectionScoreData[x])
            }
        }

        if (detectedRotatedRects.isEmpty() || detectedConfidences.isEmpty()) {
            return null
        }

        val boundingBoxesMat =
            MatOfRotatedRect(Converters.vector_RotatedRect_to_Mat(detectedRotatedRects))
        val detectedConfidencesMat =
            MatOfFloat(Converters.vector_float_to_Mat(detectedConfidences))

        Dnn.NMSBoxesRotated(
            boundingBoxesMat,
            detectedConfidencesMat,
            detectionConfidenceThreshold.toFloat(),
            detectionNMSThreshold.toFloat(),
            indicesMat
        )

        return DetectionResult(boundingBoxesMat, indicesMat,
            ratioWidth, ratioHeight)
    }

}