# TensorFlow Lite Examples - Android

This repository refactors and rewrites all the TensorFlow Lite examples included in the TensorFlow official website. For more details, please refer to:

[https://www.tensorflow.org/lite/examples](https://www.tensorflow.org/lite/examples)

The target of this repository is to provide you a much simpler way to use TensorFlow Lite on Android. It helps you easily understand how does a captured Bitmap, a small buffer of raw audio data, or a piece of text convert run in inference and how to represent those results on the user interface.

The first step of this repository is almost accomplished. I have cleaned up the source codes from the official examples and simply refactor them to remove duplicated and low-performance parts. The next step is to refactor the code deeply, extract more reusable parts, and create a support library.

Here is an overview of the progress of each standalone example:

Examples             | Status         | Inference Performance
:-------:            | :-:            | :--
Image Classification | CLEANED UP     | 25 ms per frame
Object Detection     | CLEANED UP     | 20 ms per frame
Pose Estimation      | CLEANED UP     | 75 ms per frame
Speech Recognition   | CLEANED UP     | 40 ms per 1.6K audio data
Gesture Recognition  | NOT WORK WELL  | 10 ms per 1.6K audio data
Smart Reply          | CLEANED UP     | 25 ms per sentence
Image Segmentation   | CLEANED UP     | 70 ms per frame
Style Transfer       | CLEANED UP     | 150 ms per frame
Digit Classifier     | CLEANED UP     | 6 ms per frame
Text Classification  | CLEANED UP     | 10 ms per frame
Q&A (BERT)           | CLEANED UP     | 280 ms per frame (Include pre-process time)

The results above are tested on Oneplus 7 (Snapdragon 855 + 128G RAM) wit TensorFlow Lite Nightly Build library.

## License

    Copyright 2020 Daily Studio.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
