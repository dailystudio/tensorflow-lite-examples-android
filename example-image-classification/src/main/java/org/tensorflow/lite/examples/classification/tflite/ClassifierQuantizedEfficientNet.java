/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

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

package org.tensorflow.lite.examples.classification.tflite;

import android.content.Context;

import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.model.Model.Device;

import java.io.IOException;

/** This TensorFlow Lite classifier works with the quantized EfficientNet model. */
public class ClassifierQuantizedEfficientNet extends Classifier {

  /**
   * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
   * bypass the normalization.
   */
  private static final float IMAGE_MEAN = 0.0f;

  private static final float IMAGE_STD = 1.0f;

  /** Quantized MobileNet requires additional dequantization to the output probability. */
  private static final float PROBABILITY_MEAN = 0.0f;

  private static final float PROBABILITY_STD = 255.0f;

  /**
   * Initializes a {@code ClassifierQuantizedMobileNet}.
   *
   * @param context
   */
  public ClassifierQuantizedEfficientNet(Context context, Device device, int numThreads, boolean useXNNPack)
      throws IOException {
    super(context, "efficientnet-lite0-int8.tflite", device, numThreads, useXNNPack);
  }

  @Override
  protected String getLabelPath() {
    return "labels_without_background.txt";
  }

  @Override
  protected TensorOperator getPreprocessNormalizeOp() {
    return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
  }

  @Override
  protected TensorOperator getPostprocessNormalizeOp() {
    return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
  }
}
