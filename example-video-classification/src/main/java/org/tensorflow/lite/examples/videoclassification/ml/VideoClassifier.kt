/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.videoclassification.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import com.dailystudio.devbricksx.development.Logger
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model
import com.dailystudio.tensorflow.litex.AssetFileLiteModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

class VideoClassifier(context: Context,
                      modelPath: String,
                      device: Model.Device = Model.Device.CPU,
                      numOfThreads: Int = 1,
                      useXNNPack: Boolean = true
) : AssetFileLiteModel(context, modelPath, device, numOfThreads, useXNNPack){

    enum class ClassifierModel {
        MOVINET_A0,
        MOVINET_A1,
        MOVINET_A2,
    }

    companion object {
        private const val IMAGE_INPUT_NAME = "image"
        private const val LOGITS_OUTPUT_NAME = "logits"
        private const val SIGNATURE_KEY = "serving_default"
        private const val INPUT_MEAN = 0f
        private const val INPUT_STD = 255f

        private const val MODEL_LABEL_FILE = "kinetics600_label_map.txt"
        private const val MODEL_MOVINET_A0_FILE = "movinet_a0_stream_int8.tflite"
        private const val MODEL_MOVINET_A1_FILE = "movinet_a1_stream_int8.tflite"
        private const val MODEL_MOVINET_A2_FILE = "movinet_a2_stream_int8.tflite"


        private val EMPTY_SHAPE = IntArray(4) {0}

        fun create(context: Context,
                   model: ClassifierModel = ClassifierModel.MOVINET_A0,
                   device: Model.Device,
                   numOfThreads: Int,
                   useXNNPack: Boolean
        ): VideoClassifier {
            val modelPath = when (model) {
                ClassifierModel.MOVINET_A0 -> { MODEL_MOVINET_A0_FILE }
                ClassifierModel.MOVINET_A1 -> { MODEL_MOVINET_A1_FILE }
                ClassifierModel.MOVINET_A2 -> { MODEL_MOVINET_A2_FILE }
            }

            return VideoClassifier(context, modelPath, device, numOfThreads, useXNNPack)
        }
    }

    private lateinit var inputShape: IntArray
    private var  outputCategoryCount: Int = 0
    private var  inputHeight: Int = 0
    private var  inputWidth: Int = 0
    private var inputState = HashMap<String, Any>()
    private val lock = Any()

    private lateinit var labels: List<String>
    private val maxResults: Int = 3

    override fun open() {
        super.open()

        val interpreter = interpreter as? Interpreter

        inputShape = interpreter
            ?.getInputTensorFromSignature(IMAGE_INPUT_NAME, SIGNATURE_KEY)
            ?.shape() ?: EMPTY_SHAPE
        outputCategoryCount = (interpreter
            ?.getOutputTensorFromSignature(LOGITS_OUTPUT_NAME, SIGNATURE_KEY)
            ?.shape() ?: EMPTY_SHAPE)[1]

        inputHeight = inputShape[2]
        inputWidth = inputShape[3]

        Logger.debug("MODEL_P: input shape: $inputShape")
        Logger.debug("MODEL_P: output count: $outputCategoryCount")
        Logger.debug("MODEL_P: inputHeight: $inputHeight")
        Logger.debug("MODEL_P: inputWidth: $inputWidth")

        labels = FileUtil.loadLabels(context, MODEL_LABEL_FILE)
        if (outputCategoryCount != labels.size)
            throw java.lang.IllegalArgumentException(
                "Label list size doesn't match with model output shape " +
                        "(${labels.size} != $outputCategoryCount"
            )
        inputState = initializeInput()

    }

    /**
     * Initialize the input objects and fill them with zeros.
     */
    private fun initializeInput(): HashMap<String, Any> {
        val inputs = HashMap<String, Any>()

        val interpreter = interpreter as? Interpreter ?: return inputs

        for (inputName in interpreter.getSignatureInputs(SIGNATURE_KEY)) {
            // Skip the input image tensor as it'll be fed in later.
            if (inputName.equals(IMAGE_INPUT_NAME))
                continue

            // Initialize a ByteBuffer filled with zeros as an initial input of the TFLite model.
            val tensor = interpreter.getInputTensorFromSignature(inputName, SIGNATURE_KEY)
            val byteBuffer = ByteBuffer.allocateDirect(tensor.numBytes())
            byteBuffer.order(ByteOrder.nativeOrder())
            inputs[inputName] = byteBuffer
        }

        return inputs
    }

    /**
     * Initialize the output objects to store the TFLite model outputs.
     */
    private fun initializeOutput(): HashMap<String, Any> {
        val outputs = HashMap<String, Any>()

        val interpreter = interpreter as? Interpreter ?: return outputs

        for (outputName in interpreter.getSignatureOutputs(SIGNATURE_KEY)) {
            // Initialize a ByteBuffer to store the output of the TFLite model.
            val tensor = interpreter.getOutputTensorFromSignature(outputName, SIGNATURE_KEY)
            val byteBuffer = ByteBuffer.allocateDirect(tensor.numBytes())
            byteBuffer.order(ByteOrder.nativeOrder())
            outputs[outputName] = byteBuffer
        }

        return outputs
    }

    /**
     * Run classify and return a list include action and score.
     */
    fun classify(inputBitmap: Bitmap): List<Category> {
        // As this model is stateful, ensure there's only one inference going on at once.
        synchronized(lock) {
            // Prepare inputs.
            val tensorImage = preprocessInputImage(inputBitmap)
            inputState[IMAGE_INPUT_NAME] = tensorImage.buffer

            // Initialize a placeholder to store the output objects.
            val outputs = initializeOutput()

            // Run inference using the TFLite model.
            (interpreter as? Interpreter)?.runSignature(inputState, outputs)

            // Post-process the outputs.
            var categories = postprocessOutputLogits(outputs[LOGITS_OUTPUT_NAME] as ByteBuffer)

            // Store the output states to feed as input for the next frame.
            outputs.remove(LOGITS_OUTPUT_NAME)
            inputState = outputs

            // Sort the output and return only the top K results.
            categories.sortByDescending { it.score }

            // Take only maxResults number of result.
            return categories.subList(0, max(maxResults, categories.size))
        }
    }

    /**
     * Return the input size required by the model.
     */
    fun getInputSize(): Size {
        return Size(inputWidth, inputHeight)
    }

    /**
     * Convert input bitmap to TensorImage and normalize.
     */
    private fun preprocessInputImage(bitmap: Bitmap): TensorImage {
        val size = min(bitmap.width, bitmap.height)

        val imageProcessor = ImageProcessor.Builder().apply {
            add(ResizeWithCropOrPadOp(size, size))
            add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
            add(NormalizeOp(INPUT_MEAN, INPUT_STD))
        }.build()
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        return imageProcessor.process(tensorImage)
    }

    /**
     * Convert output logits of the model to a list of Category objects.
     */
    private fun postprocessOutputLogits(logitsByteBuffer: ByteBuffer): MutableList<Category> {
        // Convert ByteBuffer to FloatArray.
        val logits = FloatArray(outputCategoryCount)
        logitsByteBuffer.rewind()
        logitsByteBuffer.asFloatBuffer().get(logits)

        // Convert logits into probability list.
        val probabilities = CalculateUtils.softmax(logits)

        // Append label name to form a list of Category objects.
        val categories = mutableListOf<Category>()
        probabilities.forEachIndexed { index, probability ->
            categories.add(Category(labels[index], probability))
        }
        return categories
    }

    /**
     * Clear the internal state of the model.
     *
     * Call this function if the future inputs is unrelated to the past inputs. (e.g. when changing
     * to a new video sequence)
     */
    fun reset() {
        // Ensure that no inference is running when the state is being cleared.
        synchronized(lock) {
            inputState = initializeInput()
        }
    }
}
