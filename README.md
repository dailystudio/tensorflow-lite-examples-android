# TensorFlow Lite Examples - Android

This repository refactors and rewrites all the TensorFlow Lite examples included in the TensorFlow official website. For more details, please refer to:

https://www.tensorflow.org/lite/examples

The target of this repository is to provide you a much simpler way to use TensorFlow Lite on Android. It helps you easily understand how does a captured Bitmap, a small buffer of raw audio data, or a piece of text convert run in inference and how to represent those results on the user interface.

The first step of this repository is almost accomplished. I have cleaned up the source codes from the official examples and simply refactor them to remove duplicated and low-performance parts. The next step is to refactor the code deeply, extract more reusable parts, and create a support library.
