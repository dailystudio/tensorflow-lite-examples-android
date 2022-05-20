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
import android.util.Pair;

import org.tensorflow.lite.support.model.Model;

import java.io.IOException;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 *
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
public class ObjectDetectionModel extends Detector {

  public static final int TF_OD_API_INPUT_SIZE = 300;

  private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
  private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
  private static final Boolean TF_OD_API_IS_QUANTIZED = true;

  // Only return this many results.
  private static final int NUM_DETECTIONS = 10;
  // Float model
  private static final float IMAGE_MEAN = 128.0f;
  private static final float IMAGE_STD = 128.0f;

  /**
   * Initializes a {@code Classifier}.
   */
  public ObjectDetectionModel(Context context, Model.Device device, int numThreads, boolean useXNNPack) throws IOException {
    super(context, TF_OD_API_MODEL_FILE, device, numThreads, useXNNPack);
  }

  @Override
  public Boolean isQuantized() {
    return TF_OD_API_IS_QUANTIZED;
  }

  @Override
  protected String getLabelPath() {
    return TF_OD_API_LABELS_FILE;
  }

  @Override
  protected Pair<Float, Float> getPreprocessNormalizeProps() {
    return new Pair(IMAGE_MEAN, IMAGE_STD);
  }

  @Override
  public int getNumOfDetections() {
    return NUM_DETECTIONS;
  }

}

