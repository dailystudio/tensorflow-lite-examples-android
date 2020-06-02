package com.dailystudio.tflite.example.image.classification.fragment

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.tflite.example.common.fragment.ExampleCameraFragment
import java.nio.ByteBuffer

private class ImageClassificationAnalyzer() : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {
        Logger.debug("dimen: ${image.width} * ${image.height}")

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        Logger.debug("Average luminosity: $luma")

        image.close()
    }
}

class ImageClassificationFragment : ExampleCameraFragment() {

    override fun createAnalyzer(): ImageAnalysis.Analyzer {
        return ImageClassificationAnalyzer()
    }

    override fun getDesiredPreviewSize(): Size {
        return Size(640, 480)
    }

}