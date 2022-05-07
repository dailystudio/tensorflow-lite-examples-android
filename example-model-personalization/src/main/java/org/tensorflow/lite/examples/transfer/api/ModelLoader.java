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

package org.tensorflow.lite.examples.transfer.api;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Handles loading the trained multiple signature model stored as a directory under Android assets.
 */
public class ModelLoader {

  private AssetManager assetManager;
  private String directoryName;

  /**
   * Create a loader for a transfer learning model under given directory.
   *
   * @param directoryName path to model directory in assets tree.
   */
  public ModelLoader(Context context, String directoryName) {
    this.directoryName = directoryName;
    this.assetManager = context.getAssets();
  }

  protected MappedByteBuffer loadMappedFile(String filePath) throws IOException {
    AssetFileDescriptor fileDescriptor = assetManager.openFd(this.directoryName + "/" + filePath);

    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(MapMode.READ_ONLY, startOffset, declaredLength);
  }
}
