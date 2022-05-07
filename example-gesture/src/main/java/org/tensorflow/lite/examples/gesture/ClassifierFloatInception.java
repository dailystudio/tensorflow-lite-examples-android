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

package org.tensorflow.lite.examples.gesture;

import android.content.Context;

import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.model.Model.Device;

import java.io.IOException;

/** This TensorFlowLite classifier works with the float EfficientNet model. */
public class ClassifierFloatInception extends Classifier {

  private static final float IMAGE_MEAN = 1f;
  private static final float IMAGE_STD = 127.0f;

  /**
   * Float model does not need dequantization in the post-processing. Setting mean and std as 0.0f
   * and 1.0f, repectively, to bypass the normalization.
   */
  private static final float PROBABILITY_MEAN = 0.0f;

  private static final float PROBABILITY_STD = 1.0f;

  private static final String MODEL_PATH = "model.tflite";

  /**
   * Initializes a {@code ClassifierFloatMobileNet}.
   *
   * @param context
   */
  public ClassifierFloatInception(Context context, Device device, int numThreads)
      throws IOException {
    super(context, MODEL_PATH, device, numThreads);
  }

  @Override
  protected String getModelPath() {
    // you can download this file from
    // see build.gradle for where to obtain this file. It should be auto
    // downloaded into assets.
    return MODEL_PATH;
  }

  @Override
  protected String getLabelPath() {
    return "labels.txt";
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
