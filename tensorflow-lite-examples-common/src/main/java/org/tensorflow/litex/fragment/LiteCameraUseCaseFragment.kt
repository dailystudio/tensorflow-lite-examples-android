package org.tensorflow.litex.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.dailystudio.tflite.example.common.image.ImageInferenceInfo

import android.os.Bundle
import android.os.Environment
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.camera.CameraFragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ImageUtils.toBitmap
import com.dailystudio.tflite.example.common.R
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.litex.LiteUseCase
import org.tensorflow.litex.getLiteUseCaseViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class ImageLiteUseCase<Output, Info: ImageInferenceInfo>
    : LiteUseCase<ImageProxy, Output, Info>() {

    var rotation: Int = 0
    var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    @Synchronized
    fun updateImageInfo(newRotation: Int,
                        newLensFacing: Int) {
        rotation = newRotation
        lensFacing = newLensFacing
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun runInference(input: ImageProxy, info: Info): Output? {
        var results: Output? = null
        info.apply {
            imageSize = Size(input.width, input.height)
            imageRotation = input.imageInfo.rotationDegrees
            cameraLensFacing = lensFacing
            screenRotation = rotation
        }

        input.image?.let {
            val frameBitmap: Bitmap? = it.toBitmap()
            val inferenceBitmap: Bitmap? = preProcessImage(frameBitmap, info)

            inferenceBitmap?.let { bitmap ->
                info.inferenceImageSize = Size(bitmap.width, bitmap.height)

                results = analyzeFrame(bitmap, info)
            }
        }

        input.close()

        return results
    }

    protected open fun preProcessImage(frameBitmap: Bitmap?,
                                       info: Info): Bitmap? {
        return frameBitmap
    }

    protected fun dumpIntermediateBitmap(bitmap: Bitmap,
                                         filename: String) {
        if (!isDumpIntermediatesEnabled()) {
            return
        }

        saveIntermediateBitmap(bitmap, filename)
    }

    protected fun saveIntermediateBitmap(bitmap: Bitmap,
                                         filename: String) {
        val dir = GlobalContextWrapper.context?.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )

        val file = File(dir, filename)
        val ret = ImageUtils.saveBitmap(bitmap, file)
        Logger.debug("intermediate saved [$ret] in [${file.absolutePath}]")
    }

    protected open fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

    abstract fun analyzeFrame(inferenceBitmap: Bitmap,
                              info: Info): Output?
}

abstract class LiteCameraUseCaseFragment: CameraFragment() {

    private lateinit var analyzerExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyzerExecutor = Executors.newSingleThreadExecutor()
    }

    protected open fun getSettingsPreference(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

    protected open fun getImageAnalysisBuilder(screenAspectRatio: Int, rotation: Int): ImageAnalysis.Builder {
        return ImageAnalysis.Builder().apply {
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(rotation)
        }
    }

    override fun buildUseCases(screenAspectRatio: Int, rotation: Int): MutableList<UseCase> {
        val cases = super.buildUseCases(screenAspectRatio, rotation)

        val imageAnalyzer = getImageAnalysisBuilder(screenAspectRatio, rotation)
            .build()
            .also {
                namesOfLiteUseCase.forEach { name ->
                    (LiteUseCase.getLiteUseCase(name) as? ImageLiteUseCase<*, *>)?.updateImageInfo(
                        rotation, lensFacing)
                }

                it.setAnalyzer(analyzerExecutor) { image ->
                    namesOfLiteUseCase.forEach { name ->
                        val viewModel = getLiteUseCaseViewModel(
                            name)
                        viewModel?.performUseCase(image)
                    }
                }
            }

        cases.add(imageAnalyzer)

        return cases
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_example
    }

    fun getCurrentLensFacing(): Int {
        return lensFacing
    }

    fun changeLensFacing(lensFacing: Int) {
        setCameraLens(lensFacing)
    }

    abstract val namesOfLiteUseCase: Array<String>

}