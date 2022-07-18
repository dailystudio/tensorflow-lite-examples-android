# TensorFlow Lite Examples - Android

This repository refactors and rewrites all the TensorFlow Lite examples for Android. They are officially illustrated on the TensorFlow official website. For more details, please refer to:

[https://www.tensorflow.org/lite/examples](https://www.tensorflow.org/lite/examples)

Most of these examples are maintained by Googlers. But of all the maintainers are experts in Android. The quality and readability are not good enough. This repository's target is to recreate these examples, with the same UI designs, with the same approaches to run inference, and provide you base facilities to do machine learning in a much simpler way on Android. It helps you quickly understand how a captured Bitmap, a small buffer of raw audio data, or a piece of text are converted to run in inferences and how to represent results on the user interface.

[![](.github/youtube.png)](https://www.youtube.com/watch?v=ctn-t1pg9pA&feature=youtu.be)

## Examples

Currently, this repository has covered almost all the examples on the official website. The rest of them are still under development. All of these examples are synchronized periodically with the official repository. 

Here is a list of the covered cases:

### Vision
- [Image Classification](./example-image-classification) 
	
	Test an image classification solution with a pre-trained model that can recognize 1000 different types of items from input frames on a mobile camera.

- [Object Detection](./example-object-detection)

	Test an image classification solution with a pre-trained model that can recognize 1000 different types of items from input frames on a mobile camera.

- [Pose estimation](./example-posenet)
	
	Explore an app that estimates the poses of people in an image.

- [Gesture recognition](./example-gesture)
	
	Train a neural network to recognize gestures caught on your webcam using TensorFlow.js, then use TensorFlow Lite to convert the model to run inference on your device.

- [Smart reply](./example-smart-reply)
	
	Generate reply suggestions to input conversational chat messages.

- [Image segmentation](./example-image-segmentation)
	
	Predict whether each pixel of an image is associated with a certain class. Trained with people, places, animals, and more.

- [Style transfer](./example-style-transfer)
	
	Apply any styles on an input image to create a new artistic image.

- [Digit classifier](./example-digit-classifier)
	
	Use a TensorFlow Lite model to classify your handwritten digits.
- [Video classification](./example-video-classification)
	
	Identify human actions in video footage.

- [Super resolution](./example-super-resolution)
	
	Generate a super-resolution image from a low-resolution image.

- [Optical character recognition](./example-optical-character-recognition)
	
	Extract texts from images using Optical Character Recognition with TensorFlow Lite.

### Natural Language

- [Text classification](./example-text-classification)
	
	Categorize free text into predefined groups. Potential applications include abusive content moderation, tone detection, and more.

- [Question and answer](./example-bert-qa)
	
	Answer user queries based on information extracted from a given text archive.

### Others

- [Speech recognition](./example-speech-recognition)

	Explore an app that uses a microphone to spot keywords and return a probability score for the words spoken.

- [Reinforcement learning](./example-reinforcement-learning)
	
	Train a game agent using reinforcement learning and build an Android game using TensorFlow Lite.

- [On-device training](./example-model-personalization)
	
	Train a TensorFlow Lite model on-device.

## TensorFlow Lite eXetremes

[TensorFlow Lite eXetrems](https://search.maven.org/artifact/cn.dailystudio/tensorflow-litex) is an open-source library that is just extracted during the recreation of the examples in this repo. It helps you build machine learning tasks in Android apps with less work wasted on repetitive routines, like permission handling, Camera setup, acceleration selection, inference statistics and show up, etc.

For more detailed information, pelase refer to [tensorflow-litex](./tensorflow-litex) in this repo.


## Templates
To make it easy to create your new example application, there are a few of boilerplate projects under the [templates](./templates) directory.

- [example-template](./templates/example-template), an empty project with the same basic UI as other examples in the repository. You can build your TensorFlow Lite example from scratch. 

- [example-template-with-litex](./templates/example-template), an empty project with the same basic UI as other examples in the repository. You can build your TensorFlow Lite example from scratch. Compare to the one above, it uses [a pre-built library](https://search.maven.org/artifact/cn.dailystudio/tensorflow-litex/1.4.5/aar) in the Maven Central instead of a module project "tensorflow-litex" in this repository.

- [example-image-template](./templates/example-image-template), more than providing the same design style as examples in the repository, it also includes basic facilities that support camera features. You can build your TensorFlow Lite example that requires Camera support.

- [example-image-template-with-litex](./templates/example-image-template), more than providing the same design style as examples in the repository, it also includes basic facilities that support camera features. You can build your TensorFlow Lite example that requires Camera support. Compare to the one above, it uses [a pre-built library](https://search.maven.org/artifact/cn.dailystudio/tensorflow-litex/1.4.5/aar) in the Maven Central instead of a module project "tensorflow-litex" in this repository.


There is also a [script](./scripts) that helps to create your project quickly from the boilerplate.

## Performance

Here is the performance test results on two Android phones and with two different Android versions. 

Compare to the official [performance tool](https://www.tensorflow.org/lite/performance/measurement) provided by TensorFlow Lite, the results are taken in a realistic running environment, which is also affected by other parts of the application and potential hardware resource occupation.

In this test, Oneplus 7 (Snapdragon 855) is a flagship device in 2019, whereas Oneplus 9 (Snapdragon 888) is a flagship device in 2021. Oneplus 7 is running with Android 11, while Oneplus 9 is running with Android 12. Both of them have 12 RAM on the device.

[![](.github/performance.png)]()


## License

    Copyright 2022 Daily Studio.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
