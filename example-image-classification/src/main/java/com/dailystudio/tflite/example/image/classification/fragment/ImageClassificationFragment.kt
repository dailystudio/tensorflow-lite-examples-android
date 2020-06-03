package com.dailystudio.tflite.example.image.classification.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.ImageUtils.toBitmap
import com.dailystudio.tflite.example.common.fragment.ExampleCameraFragment
import org.tensorflow.lite.examples.classification.tflite.Classifier
import org.tensorflow.lite.examples.classification.tflite.Classifier.Recognition
import java.nio.ByteBuffer
import kotlin.math.min

private class ImageClassificationAnalyzer(private val rotation: Int) : ImageAnalysis.Analyzer {

    private var rgbFrameBitmap: Bitmap? = null
    private var classifier: Classifier? = null

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        Logger.debug("image dimen: ${image.width} x ${image.height}")
        Logger.debug("rotation: image = ${image.imageInfo.rotationDegrees}, screen = $rotation")
        image.image?.let { image ->
            rgbFrameBitmap = image.toBitmap()
            rgbFrameBitmap?.let { bitmap ->
                val rotatedBitmap = ImageUtils.rotateBitmap(bitmap, rotation)
                val cropSize: Int = min(bitmap.width, bitmap.height)

                if (classifier == null) {
                    val context = GlobalContextWrapper.context
                    context?.let {
                        classifier = Classifier.create(it,
                            Classifier.Model.QUANTIZED_MOBILENET,
                            Classifier.Device.CPU,
                            1)
                    }

                    Logger.debug("classifier created: $classifier")
                }

                classifier?.let {
                    val start = System.currentTimeMillis()
                    val results: List<Recognition> =
                        it.recognizeImage(rgbFrameBitmap, rotation)
                    val end = System.currentTimeMillis()
                    Logger.info("Detect[in ${end - start} ms]: %s", results)
                }
            }
        }

        image.close()
    }
}

class ImageClassificationFragment : ExampleCameraFragment() {

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int): ImageAnalysis.Analyzer {
        return ImageClassificationAnalyzer(rotation)
    }

    override fun getDesiredPreviewSize(): Size {
        return Size(640, 480)
    }

}