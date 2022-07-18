# TensorFlow LiteX
[![License](https://poser.pugx.org/dreamfactory/dreamfactory/license.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.dailystudio/tensorflow-litex/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.dailystudio/tensorflow-litex)

TensorFlow Lite eXtremes, as *TensorFlow LiteX* below, is a lightweight library which encapsulates commonly used routines and provides you a set of shortcuts when you do machine learning task on Android platform. 

It is a pretty slim layer above the TensorFlow Lite Support library. Without any latency increment, it removes repetitive codes which you usually use to open models, prepare data, and display results, especially for those cases that require image input from an on-device Camera.

## Quick Setup
To use **TensorFlow LiteX** in your application, follow the steps below.

###Step 1: Dependencies 

Add the following dependencies in build.gradle of your application.

```groovy
repositories { 
    mavenCentral()
}

dependencies {
    implementation "cn.dailystudio:tensorflow-litex:$tfiltex_version"
    
    // Some boilerplates in TensorFlow LiteX require this compiler for code generation
    kapt "cn.dailystudio:devbricksx-kotlin-compiler:$devbricksx_version"
}
```

#### Latest version

```groovy
tfiltex_version = "1.4.5"
devbricksx_version = "1.6.5"
```

###Step 2: Configure compile options
Add the following compile options in build.gradle of your application module.

```groovy
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}

kotlinOptions {
    jvmTarget = "1.8"
}

aaptOptions {
    noCompress "tflite"
}

```

## Power your app with ML features
When you correctly set up the right dependencies of TensorFlow LiteX, you are ready to add machine learning features to your app. Follow the steps below, you can introduce a new ML feature in only a few minutes. 


### 1. Prepare TensorFlow Lite models
No matter you get it from your professional teams or download it from open source sites, such as TensorFlow Hub, make sure it can work on your target devices with acceptable performance. These works are out of the scope of this document. You can refer to [official document](https://www.tensorflow.org/lite/guide#1_generate_a_tensorflow_lite_model) for details.

In TensorFlow LiteX, it use **LiteModel** to represent a TensorFlow Lite model. Usually, the TensorFlow Lite models are put in **assets** folders of the application. You can load it with the shortcut API of TensorFlow LiteX. 

```kotlin
val model = LiteModel.fromAssetFile(context,
    modelPath = "model.tflite",
    device = Model.Device.GPU, 
    numOfThreads = 4,
    useXNNPack = true
)

```

It illustrates how to load a model, named "model.tflte", in **assets** directory. It also overrides the hardware delegation properly by using *device*, *numOfThreads*, and *useXNNPack* parameters.

There is no limitation on the location where you put your model in. You can download it during the runtime and put it in your internal or external storage. Use **LiteModel.fromBuffer()** to load it from other place rather than assets directory.

### 2. Use models in use cases
Basically, after you load the model into memory through **LiteModel**, you can use it to run inference in your apps. For example,

```kotlin
interpreter.runForMultipleInputsOutputs(inputArray, outputMap)
```

**LiteModel** contains a protected member, named **interpreter**. It is an instance of **[InterpreterApi](https://www.tensorflow.org/lite/api_docs/java/org/tensorflow/lite/InterpreterApi)** which is the core facility of the TensorFlow Lite library. How to use it to run inferences is not the responsibility of TensorFlow LiteX, since it is quite different in different cases, and difficult to create any encapsulation on it.

But, fortunately, there is still something we can do. Practically, in on-device ML, the codes of core inference are only a very small part. Most codes are used to prepare the inputs, convert outputs and reload the models. And, nearly half of them are repetitive, especially for the part of model recreation after related settings are changed.

According to the situation described above, TensorFlow LiteX uses **LiteUseCase** to present a dedicated ML use case. It simplifies the codes that are related to those duplicated tasks. 

The **LiteUseCase** is an abstract class. You need to derive from it and implement your own staff. As you can see below, there are three abstract function you have to implement:

```kotlin

abstract class LiteUseCase<Input, Output, Info: InferenceInfo> {

	...
	
    @WorkerThread
    protected abstract fun createModels(
        context: Context,
        device: Model.Device = Model.Device.CPU,
        numOfThreads: Int = 1,
        useXNNPack: Boolean = true,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel>

    abstract fun createInferenceInfo(): Info

    @WorkerThread
    protected abstract fun runInference(input: Input, info: Info): Output?
}

```
### createModels()
It implements all the instances of LiteModel that you required in your use case. No matter how to create a specific LiteModel, you must return an array that holds these model instances. Here is an example of creating model in Digit Classifier,

```kotlin

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(DigitClassifier(context, device, numOfThreads, useXNNPack))
    }


```

### createInferenceInfo()
To create an instance of inference information of the use case. TensorFlow LiteX uses InferenceInfo and derived subclasses to describe the state of each inference in runtime, such as analysis time and real inference time, etc. The created inference information will be used repeatly across different inferences to track the updates. 

There are pre-built inhertted class of InferenceInfo, like ImageInferenceInfo. It adds more information of the image used in inference. You need to create the right instance on demand,

```kotlin

    override fun createInferenceInfo(): ImageInferenceInfo {
        return ImageInferenceInfo()
    }


```

### runInference()
The most important fucntion in LiteUseCase. It defines how to perform the inference with the models. You can use models created by createModels() here to perfom the core action in your case.

If you only have a single model in the use case, you can directly retrieve it by shortcut member **defaultModel**:

```kotlin
val result = (defaultModel as? DigitClassifier)?.classify(inferenceBitmap)
```

Or, sometimes, in a bit more complicated case, you might need more than one models. You can access them by get it from **liteModels**, which is a protected member of LiteUseCase,

```kotlin
val result = (liteModels?.get(1) as? OCRRecognitionModel)?.recognizeTexts(
	inferenceBitmap, detectionResult, ocrResults)
```

The index used to access a model in **liteModels** depends on the order you return in **createModels()**. Here is the implementation of **createModels()** related the code above. It helps you understand why we can access the **OCRRecognitionModel** by index 1.

```kotlin

    override fun createModels(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        useXNNPack: Boolean,
        settings: InferenceSettingsPrefs
    ): Array<LiteModel> {
        return arrayOf(
            OCRDetectionModel(context, device, numOfThreads, useXNNPack),
            OCRRecognitionModel(context, numOfThreads),
        )
    }


``` 

### 3. Add use cases to your app

---
> The following content are under developement
