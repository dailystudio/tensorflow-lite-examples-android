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
==============================================================================*/

package com.dailystudio.tflite.example.transfer.model;

import android.content.Context;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.litex.TFLiteModel;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/** A wrapper for TFLite model with multiple signature runner. */
public class LiteMultipleSignatureModel extends TFLiteModel {

  private static final int BOTTLENECK_SIZE = 7 * 7 * 1280;
  private static final int EXPECTED_BATCH_SIZE = 20;

  private final int numClasses;

  LiteMultipleSignatureModel(Context context,
                             String modelPath,
                             Model.Device device,
                             int numOfThreads,
                             boolean useXNNPack,
                             int numOfClasses) {
    super(context, modelPath, device, numOfThreads, useXNNPack);
    this.numClasses = numOfClasses;
  }

  /**
   * Loads the bottleneck feature from the given image array.
   *
   * @param image 3-D float array of size (IMG_SIZE, IMG_SIZE, 3)
   * @return 1-D float array containing bottleneck features
   */
  float[] loadBottleneck(float[][][] image) {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("feature", new float[][][][] {image});
    Map<String, Object> outputs = new HashMap<>();
    float[][] bottleneck = new float[1][BOTTLENECK_SIZE];
    outputs.put("bottleneck", bottleneck);
    getInterpreter().runSignature(inputs, outputs, "load");
    return bottleneck[0];
  }

  /**
   * Runs one training step with the given bottleneck batches and labels.
   *
   * @param bottlenecks 2-D float array of bottleneck batches of size (BATCH_SIZE, BOTTLENECK_SIZE)
   * @param labels 2-D float array of label batches of size (BATCH_SIZE, NUM_CLASSES)
   * @return the training loss
   */
  float runTraining(float[][] bottlenecks, float[][] labels) {
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("bottleneck", bottlenecks);
    inputs.put("label", labels);

    Map<String, Object> outputs = new HashMap<>();
    FloatBuffer loss = FloatBuffer.allocate(1);
    outputs.put("loss", loss);

    getInterpreter().runSignature(inputs, outputs, "train");

    return loss.get(0);
  }

  /**
   * Invokes inference on the given image batches.
   *
   * @param testImage 3-D float array of image of size (IMG_SIZE, IMG_SIZE, 3)
   * @return 1-D float array of softmax output of prediction
   */
  float[] runInference(float[][][] testImage) {
    // Run the inference.
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("feature", new float[][][][] {testImage});

    Map<String, Object> outputs = new HashMap<>();
    float[][] output = new float[1][numClasses];
    outputs.put("output", output);
    getInterpreter().runSignature(inputs, outputs, "infer");
    return output[0];
  }

  int getExpectedBatchSize() {
    return EXPECTED_BATCH_SIZE;
  }

  int getNumBottleneckFeatures() {
    return getInterpreter().getInputTensorFromSignature("bottleneck", "train").shape()[1];
  }

}
