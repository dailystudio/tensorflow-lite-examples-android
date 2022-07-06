/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

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

package org.tensorflow.lite.examples.detection.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Pair;

import com.dailystudio.devbricksx.development.Logger;

import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.litex.AssetFileLiteModel;
import org.tensorflow.litex.TFLiteModel;
import org.tensorflow.litex.image.Recognition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Generic interface for interacting with different recognition engines. */
abstract public class Detector extends AssetFileLiteModel {
  /** Labels corresponding to the output of the vision model. */
  private List<String> labels;

  private boolean isModelQuantized;
  // Config values.
  private int inputSize;
  // Pre-allocated buffers.
  private int[] intValues;
  // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
  // contains the location of detected boxes
  private float[][][] outputLocations;
  // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
  // contains the classes of detected boxes
  private float[][] outputClasses;
  // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
  // contains the scores of detected boxes
  private float[][] outputScores;
  // numDetections: array of shape [Batchsize]
  // contains the number of detected boxes
  private float[] numDetections;

  private ByteBuffer imgData;

  public Detector(Context context, String modelPath, Model.Device device, int numOfThreads, boolean useXNNPack) throws IOException {
    super(context, modelPath, device, numOfThreads, useXNNPack);
  }

  @Override
  public void open() {
    super.open();

    try {
      labels = FileUtil.loadLabels(getContext(), getLabelPath());
    } catch (IOException e) {
      Logger.INSTANCE.error("failed to load labels from ["
              + getLabelPath() + "]: " + e);
    }

    InterpreterApi tfLiteInterpreter = getInterpreter();

    int imageTensorIndex = 0;
    int[] imageShape = tfLiteInterpreter.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}

    inputSize = imageShape[1];

    isModelQuantized = isQuantized();
    // Pre-allocate buffers.
    int numBytesPerChannel;
    if (isModelQuantized) {
      numBytesPerChannel = 1; // Quantized
    } else {
      numBytesPerChannel = 4; // Floating point
    }

    imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * numBytesPerChannel);
    imgData.order(ByteOrder.nativeOrder());
    intValues = new int[inputSize * inputSize];

    int numOfDetections = getNumOfDetections();
    outputLocations = new float[1][numOfDetections][4];
    outputClasses = new float[1][numOfDetections];
    outputScores = new float[1][numOfDetections];
    numDetections = new float[1];
  }

  public List<Recognition> recognizeImage(final Bitmap bitmap) {
    // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    // Preprocess the image data from 0-255 int to normalized float based
    // on the provided parameters.
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    Pair<Float, Float> props = getPreprocessNormalizeProps();

    float imageMean = props.first;
    float imageStd = props.second;

    imgData.rewind();
    for (int i = 0; i < inputSize; ++i) {
      for (int j = 0; j < inputSize; ++j) {
        int pixelValue = intValues[i * inputSize + j];
        if (isModelQuantized) {
          // Quantized model
          imgData.put((byte) ((pixelValue >> 16) & 0xFF));
          imgData.put((byte) ((pixelValue >> 8) & 0xFF));
          imgData.put((byte) (pixelValue & 0xFF));
        } else { // Float model
          imgData.putFloat((((pixelValue >> 16) & 0xFF) - imageMean) / imageStd);
          imgData.putFloat((((pixelValue >> 8) & 0xFF) - imageMean) / imageStd);
          imgData.putFloat(((pixelValue & 0xFF) - imageMean) / imageStd);
        }
      }
    }
    Trace.endSection(); // preprocessBitmap

    int numOfDetection = getNumOfDetections();
    // Copy the input data into TensorFlow.
    Trace.beginSection("feed");
    outputLocations = new float[1][numOfDetection][4];
    outputClasses = new float[1][numOfDetection];
    outputScores = new float[1][numOfDetection];
    numDetections = new float[1];

    Object[] inputArray = {imgData};
    Map<Integer, Object> outputMap = new HashMap<>();
    outputMap.put(0, outputLocations);
    outputMap.put(1, outputClasses);
    outputMap.put(2, outputScores);
    outputMap.put(3, numDetections);
    Trace.endSection();

    // Run the inference call.
    Trace.beginSection("run");
    getInterpreter().runForMultipleInputsOutputs(inputArray, outputMap);
    Trace.endSection();

    // Show the best detections.
    // after scaling them back to the input size.

    // You need to use the number of detections from the output and not the NUM_DETECTONS variable declared on top
    // because on some models, they don't always output the same total number of detections
    // For example, your model's NUM_DETECTIONS = 20, but sometimes it only outputs 16 predictions
    // If you don't use the output's numDetections, you'll get nonsensical data
    int numDetectionsOutput = Math.min(numOfDetection, (int) numDetections[0]); // cast from float to integer, use min for safety

    final ArrayList<Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
    for (int i = 0; i < numDetectionsOutput; ++i) {
      final RectF detection =
              new RectF(
                      outputLocations[0][i][1] * inputSize,
                      outputLocations[0][i][0] * inputSize,
                      outputLocations[0][i][3] * inputSize,
                      outputLocations[0][i][2] * inputSize);
      // SSD Mobilenet V1 Model assumes class 0 is background class
      // in label file and class labels start from 1 to number_of_classes+1,
      // while outputClasses correspond to class index from 0 to number_of_classes
      int labelOffset = 1;
      recognitions.add(
              new Recognition(
                      "" + i,
                      labels.get((int) outputClasses[0][i] + labelOffset),
                      outputScores[0][i],
                      detection));
    }
    Trace.endSection(); // "recognizeImage"
    return recognitions;
  }

  abstract public Boolean isQuantized();
  abstract public int getNumOfDetections();

  /** Gets the name of the label file stored in Assets. */
  protected abstract String getLabelPath();
  /** Gets the TensorOperator to nomalize the input image in preprocessing. */
  protected abstract Pair<Float, Float> getPreprocessNormalizeProps();

}
