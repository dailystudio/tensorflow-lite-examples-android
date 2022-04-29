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
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.examples.ocr.ImageUtils
import org.tensorflow.lite.examples.ocr.ModelExecutionResult
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.TFLiteModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OpticalCharacterRecognitionModel(
    context: Context,
    device: Model.Device,
    numOfThreads: Int
): TFLiteModel(context, arrayOf(DETECTION_TF_MODEL_PATH, RECOGNITION_TF_MODEL_PATH)
    , arrayOf(Model.Device.GPU, Model.Device.CPU), arrayOf(4, 4)) {

    companion object {
        const val DETECTION_TF_MODEL_PATH = "text_detection.tflite"
        const val RECOGNITION_TF_MODEL_PATH = "text_recognition.tflite"

        private const val alphabets = "0123456789abcdefghijklmnopqrstuvwxyz"
        private const val displayImageSize = 257
        private const val detectionImageHeight = 320
        private const val detectionImageWidth = 320
        private val detectionImageMeans =
            floatArrayOf(103.94.toFloat(), 116.78.toFloat(), 123.68.toFloat())
        private val detectionImageStds = floatArrayOf(1.toFloat(), 1.toFloat(), 1.toFloat())
        private val detectionOutputNumRows = 80
        private val detectionOutputNumCols = 80
        private val detectionConfidenceThreshold = 0.5
        private val detectionNMSThreshold = 0.4
        private const val recognitionImageHeight = 31
        private const val recognitionImageWidth = 200
        private const val recognitionImageMean = 0.toFloat()
        private const val recognitionImageStd = 255.toFloat()
        private const val recognitionModelOutputSize = 48
    }

    private val recognitionResult: ByteBuffer = ByteBuffer.allocateDirect(recognitionModelOutputSize * 8)
    private var ratioHeight = 0.toFloat()
    private var ratioWidth = 0.toFloat()
    private var indicesMat: MatOfInt
    private var boundingBoxesMat: MatOfRotatedRect
    private var ocrResults: HashMap<String, Int> = HashMap()


    private val detectionInterpreter: Interpreter?
        get() {
            return getInterpreter(0)
        }

    private val recognitionInterpreter: Interpreter?
        get() {
            return getInterpreter(1)
        }
    init {
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
        indicesMat = MatOfInt()
        boundingBoxesMat = MatOfRotatedRect()
        ocrResults = HashMap()
    }

    fun analyze(bitmap: Bitmap): ModelExecutionResult? {
        try {
            ratioHeight = bitmap.height.toFloat() / detectionImageHeight
            ratioWidth = bitmap.width.toFloat() / detectionImageWidth
            ocrResults.clear()

            var detectTime = 0L
            var recognizeTime = 0L

            val dStartTime = System.currentTimeMillis()
            detectTexts(bitmap)
            val dEndTime = System.currentTimeMillis()

            detectTime = dEndTime - dStartTime

            val rStartTime = System.currentTimeMillis()
            val bitmapWithBoundingBoxes = recognizeTexts(bitmap, boundingBoxesMat, indicesMat)
            val rEndTime = System.currentTimeMillis()

            recognizeTime = rEndTime - rStartTime

            return ModelExecutionResult(bitmapWithBoundingBoxes, "OCR result", ocrResults).apply {
                this.detectionTime = detectTime
                this.recognitionTime = recognizeTime
            }
        } catch (e: Exception) {
            val exceptionLog = "something went wrong: ${e.message}"
            e.printStackTrace()
            Logger.error(exceptionLog)

            return ModelExecutionResult(bitmap, exceptionLog, HashMap())
        }
    }

    private fun detectTexts(data: Bitmap) {
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

        detectionInterpreter?.runForMultipleInputsOutputs(detectionInputs, detectionOutputs)

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

        val detectedConfidencesMat = MatOfFloat(Converters.vector_float_to_Mat(detectedConfidences))

        boundingBoxesMat = MatOfRotatedRect(Converters.vector_RotatedRect_to_Mat(detectedRotatedRects))
        Dnn.NMSBoxesRotated(
            boundingBoxesMat,
            detectedConfidencesMat,
            detectionConfidenceThreshold.toFloat(),
            detectionNMSThreshold.toFloat(),
            indicesMat
        )
    }

    private fun recognizeTexts(
        data: Bitmap,
        boundingBoxesMat: MatOfRotatedRect,
        indicesMat: MatOfInt
    ): Bitmap {
        val bitmapWithBoundingBoxes = data.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmapWithBoundingBoxes)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10.toFloat()
        paint.setColor(Color.GREEN)

        for (i in indicesMat.toArray()) {
            val boundingBox = boundingBoxesMat.toArray()[i]
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
            recognitionInterpreter?.run(recognitionTensorImage.buffer, recognitionResult)

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