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

package org.tensorflow.lite.examples.smartreply;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.litex.AssetFileLiteModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** Interface to load TfLite model and provide predictions. */
public class SmartReplyClient extends AssetFileLiteModel {
  private static final String TAG = "SmartReplyDemo";
  private static final String MODEL_PATH = "smartreply.tflite";
  private static final String BACKOFF_PATH = "backoff_response.txt";
  private static final String JNI_LIB = "smartreply_jni";

  private long storage;
  private volatile boolean isLibraryLoaded;

  public SmartReplyClient(@NonNull Context context) {
    super(context, MODEL_PATH, Model.Device.CPU, 1, false);
  }

  public boolean isLoaded() {
    return storage != 0;
  }

  @Override
  public void open() {
    if (!isLibraryLoaded) {
      System.loadLibrary(JNI_LIB);
      isLibraryLoaded = true;
    }

    try {
      MappedByteBuffer mappedByteBuffer = FileUtil.loadMappedFile(getContext(), getModelPath());

      String[] backoff = loadBackoffList();
      storage = loadJNI(mappedByteBuffer, backoff);
    } catch (IOException e) {
      Log.e(TAG, "Fail to load model", e);
    }
  }

  @WorkerThread
  public synchronized SmartReply[] predict(String[] input) {
    if (storage != 0) {
      return predictJNI(storage, input);
    } else {
      return new SmartReply[] {};
    }
  }

  @WorkerThread
  public synchronized void unloadModel() {
    close();
  }

  @Override
  public synchronized void close() {
    if (storage != 0) {
      unloadJNI(storage);
      storage = 0;
    }
  }

  private String[] loadBackoffList() throws IOException {
    List<String> labelList = new ArrayList<String>();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(getContext().getAssets().open(BACKOFF_PATH)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty()) {
          labelList.add(line);
        }
      }
    }
    String[] ans = new String[labelList.size()];
    labelList.toArray(ans);
    return ans;
  }

  @Keep
  private native long loadJNI(MappedByteBuffer buffer, String[] backoff);

  @Keep
  private native SmartReply[] predictJNI(long storage, String[] text);

  @Keep
  private native void unloadJNI(long storage);
}
