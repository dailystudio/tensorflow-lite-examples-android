# TensorFlow LiteX
[![License](https://poser.pugx.org/dreamfactory/dreamfactory/license.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.dailystudio/tensorflow-litex/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.dailystudio/tensorflow-litex)

TensorFlow Lite eXtremes, as *TensorFlow LiteX*, is a lightweight library which encapsulates commonly used routines and provides you with a set of shortcuts when you do machine learning tasks on Android platform. 

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
No matter from where you get your model,  from your professional teams or download it from open source sites, such as TensorFlow Hub, make sure it can work on your target devices with acceptable performance. These works are out of the scope of this document. You can refer to [official document](https://www.tensorflow.org/lite/guide#1_generate_a_tensorflow_lite_model) for details.

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

There is no limitation on the location where you put your model in. You can download it during the runtime and put it in your internal or external storage. Use **LiteModel.fromBuffer()** to load it from another place rather than assets directory.

### 2. Wrap models into use cases
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
It implements all the instances of LiteModel that you require in your use case. No matter how to create a specific LiteModel, you must return an array that holds these model instances. Here is an example of creating model in Digit Classifier,

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
To create an instance of inference information of the use case. TensorFlow LiteX uses InferenceInfo and derived subclasses to describe the state of each inference in runtime, such as analysis time and real inference time, etc. The created inference information will be used repeatedly across different inferences to track the updates. 

There are pre-built inherited classes of InferenceInfo, like ImageInferenceInfo. It adds more information of the image used in inference. You need to create the right instance on demand,

```kotlin
override fun createInferenceInfo(): ImageInferenceInfo {
    return ImageInferenceInfo()
}
```

### runInference()
The most important function in LiteUseCase. It defines how to perform the inference with the models. You can use models created by createModels() here to perform the core action in your case.

If you only have a single model in the use case, you can directly retrieve it by shortcut member **defaultModel**:

```kotlin
val result = (defaultModel as? DigitClassifier)?.classify(inferenceBitmap)
```

Or, sometimes, in a bit more complicated case, you might need more than one model. You can access them by get it from **liteModels**, which is a protected member of LiteUseCase,

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

### 3. Introduce use cases into your app
Now, it is time to use the case in your app. You can create an instance of your use case anywhere, just by calling its constructor. Like this,

```kotlin
val useCase = DigitClassifierUseCase()
```

With this instance, you can call **runModels()** to run an inference with loaded models and get the results. 

```kotlin
val retOfInference = useCase.runModels(image)
```
**runModels()** returns a pair of data. The value of the **first** element in the pair is the inference result, whereas the **second** one is the corresponding inference information.

```kotlin
val output = retOfInference.first
val info = retOfInference.second
```

### 4. Manage use cases in your app
Till now, you can use the LiteUseCase in your apps. But they are still in an "Unmanaged" state which means you need to handle their life cycles by yourself. You have to destroy them at the right time and recreate the models when important settings are changed. TensorFlow LiteX also provides useful facilities to manage use cases in your applications. 

There is a ViewModel, called **LiteUseCaseViewModel**, helps you manage **LiteUseCase** in your apps. You can get this ViewModel anywhere inside your fragments or activities to manage use cases.

```kotlin
val viewModel = ViewModelProvider(this)[LiteUseCaseViewModel::class.java]
```

According to the official MVVM document, **ViewModelProvider** creates different instances of the same ViewModel class in different lifecycle scopes. If you are not quite clear about the differences, or if you want to share inferences information across different fragments or activities, TensorFlow LiteX also extends **Fragment** and **AppCompatActivity**  with functions to simplify this process. 

To retrieve the instance of **LiteUseCaseViewModel** in a Fragment or Activity, just call the **getLiteUseCaseViewModel()**

```kotlin
val viewModel = getLiteUseCaseViewModel()
```

Using this instance you can manage your use case by calling its function **manageUseCase()** and you must give the case a unique name to identify it later.

```kotlin
viewModel.manageUseCase("classifier", useCase)
```
After that, you can get the use case easily with the name,

```kotlin
val useCase = viewModel.getUseCase("classifier")
```
To give developer the maximum convenience, the **LiteUseCaseViewModel** also provides a shortcut function to run inference,

```kotlin
val output = viewModel.performUseCase("classifier", image)
```
By using **LiteUseCaseViewModel**, you need to care about the destruction of use cases. Plus, by default, it automatically recreates models in managed use cases when the following settings are changed, 

- Type of hardware used for inference, CPU or GPU
- Number of threads
- Using XNNPack or not

### 5. Observe inference changes
You can retrieve inference results directly by calling **runModels()** or **performUseCase()**. But, if you want to track the results of inference information continuously somewhere in you apps, you can observe them through **LiteUseCaseViewModel**'s extended functions in **Fragment** and **AppCompatActivity**. Here is an example,

```kotlin
observeUseCaseOutput("classifier") { output ->
    output?.let {
        displayResultsOnUi(it)
    }
}
observeUseCaseInfo(("classifier") { info ->
    syncInferenceInfo(info)
}
```

## Simplify ML stuff in your app
By leveraging **LiteUseCase** and **LiteUseCaseViewModel**, you can simplify a lot of repetitive work when you do ML things in your apps. Based on these facilities, TensorFlow LiteX also offers you something to accelerate your development with TensorFlow Lite.

### LiteUseCaseActivity
Based on AppCompatActivity, it encapsulates a layer that helps you build and manage LiteUseCase, and observe inference results and information. Plus, it provides unified basic layouts of your application, such as,

- **Bottom sheet** which displays the inference results and information
- **Settings screen** which includes some default settings, e.g. hardware acceleration options, number of inference threads, and XNNPack support.
- **About screen** which also supports using a short video as the app introduction

**LiteUseCaseActivity** includes a Fragment that is responsible for ML tasks. How to run the inferences with use cases is the work of this Fragment, while **LiteUseCaseActivity** focuses on managing these use cases and monitoring the inference results and information.

To make the responsibilities clear, it is designed as an abstract class. You have to implement the following abstract functions before using this class. Using Digit Classifier as the example,

#### buildLiteUseCase()
It returns a map from **String** to **LiteUseCase**. **LiteUseCaseActivity** uses this map to help you manage these cases.

```kotlin
override fun buildLiteUseCase(): Map<String, LiteUseCase<*, *, *>> {
    return mapOf(
        DigitClassifierUseCase.UC_NAME to DigitClassifierUseCase()
    )
}
```

#### createBaseFragment()
It returns a Fragment that performs your primary ML tasks.

```kotlin
override fun createBaseFragment(): Fragment {
    return DigitClassifierFragment()
}

```

#### createResultsView()
By default, it is empty in the view of results. You can create your own layouts for the display of inference results.

But, sometimes, the results are already presented to users inside the base Fragment, so you can return null here.

```kotlin
override fun createResultsView(): View? {
    val view: View = LayoutInflater.from(this).inflate(
        R.layout.layout_results_view, null)

    digitBitmap = view.findViewById(R.id.result_image)
    resultDigit = view.findViewById(R.id.result_digit)
    resultProp = view.findViewById(R.id.result_prop)

    return view
}
```

#### onResultsUpdated()
If you have a customized view for results displaying. You can update it with the latest inference's results.

```kotlin
override fun onResultsUpdated(nameOfUseCase: String, results: Any) {
    if (results is RecognizedDigit) {
        resultDigit.text = if (results.digit != -1) {
            "%d".format(results.digit)
        } else {
            getString(R.string.prompt_draw)
        }

        resultProp.text = if (results.digit != -1) {
            "(%3.1f%%)".format(results.prop * 100)
        } else {
            ""
        }
    }
}

```

### ImageLiteUseCase
When you intend to do ML tasks related to image processing from the Camera. You can use **ImageLiteUseCase**  which inherits from **LiteUseCase**, but automatically converts image data that gets from CameraX API to **Bitmap**. It is much easier for you to perform operations on Bitmap rather than raw YUV data. 

You implement its abstract function **analyzeFrame()** instead **runInference()** in its superclass.

```kotlin
override fun analyzeFrame(
    inferenceBitmap: Bitmap,
    info: ImageInferenceInfo
): List<Recognition>? {
    var mappedResults: List<Recognition>? = null
	
    val start = System.currentTimeMillis()
    val results: List<Recognition>? =
        (defaultModel as? Detector)?.recognizeImage(inferenceBitmap)
    val end = System.currentTimeMillis()
	
    info.inferenceTime = (end - start)
	
    results?.let {
        mappedResults = mapRecognitions(it)
    }
	
    return mappedResults
}

```

### LiteCameraUseCaseFragment
As a good company of **ImageLiteUseCase**, when you are doing ML tasks with on-device cameras, you can extend **LiteCameraUseCaseFragment** to implement your base Fragment.

**LiteCameraUseCaseFragment** encapsulates lots of codes that are used to manipulate cameras with CameraX APIs. The only thing you need to do is override its abstract member **namesOfLiteUseCase**. The variable tells the Fragment to run all of these use cases when it processes each frame.

```kotlin
class ImageClassificationCameraFragment : LiteCameraUseCaseFragment() {

    override val namesOfLiteUseCase: Array<String>
        get() = arrayOf(ClassifierUseCase.UC_NAME)

}
```

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


