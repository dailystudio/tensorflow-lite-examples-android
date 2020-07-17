package com.dailystudio.tflite.example.image.styletransfer.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.lifecycle.Observer
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.app.AbsPrefs
import com.dailystudio.devbricksx.app.PrefsChange
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import com.dailystudio.devbricksx.utils.MatrixUtils
import com.dailystudio.tflite.example.common.image.AbsImageAnalyzer
import com.dailystudio.tflite.example.common.image.AbsExampleCameraFragment
import com.dailystudio.tflite.example.common.image.AdvanceInferenceInfo
import com.dailystudio.tflite.example.image.styletransfer.StyleTransferPrefs
import org.tensorflow.lite.examples.styletransfer.StyleTransferModelExecutor
import org.tensorflow.lite.examples.styletransfer.StyleTransferResult

private class StyleTransferAnalyzer(rotation: Int, lensFacing: Int)
    : AbsImageAnalyzer<AdvanceInferenceInfo, StyleTransferResult>(rotation, lensFacing) {

    companion object {

        private const val CONTENT_IMAGE_SIZE = 384

        private const val PRE_SCALED_IMAGE_FILE = "pre-scaled.png"
        private const val STYLED_IMAGE_FILE = "styled.png"
    }

    private var styleTransferModelExecutor: StyleTransferModelExecutor? = null
    private var styleBitmap: Bitmap? = null

    fun selectStyle(styleName: String) {
        styleBitmap = null
    }

    override fun analyzeFrame(inferenceBitmap: Bitmap, info: AdvanceInferenceInfo): StyleTransferResult? {
        var results: StyleTransferResult? = null
        val context = GlobalContextWrapper.context

        if (styleTransferModelExecutor == null) {
            context?.let {
                styleTransferModelExecutor =
                    StyleTransferModelExecutor(it, true)
            }

            Logger.debug("segmentation model created: $styleTransferModelExecutor")
        }

        if (styleBitmap == null) {
            context?.let {
                val styleName = StyleTransferPrefs.getSelectedStyle(context)
                styleBitmap = ImageUtils.loadAssetBitmap(it,
                    "thumbnails/${styleName}")
            }
        }

        styleTransferModelExecutor?.let { model ->
            val styledBitmap = styleBitmap?.let {
                model.fastExecute(inferenceBitmap, it , info)
            } ?: inferenceBitmap

            val trimmed = ImageUtils.trimBitmap(
                styledBitmap, info.frameSize.width.toFloat() / info.frameSize.height)
            results = StyleTransferResult(trimmed)
            dumpIntermediateBitmap(trimmed, STYLED_IMAGE_FILE)
        }

        return results
    }

    override fun createInferenceInfo(): AdvanceInferenceInfo {
       return AdvanceInferenceInfo()
    }

    override fun preProcessImage(frameBitmap: Bitmap?,
                                 info: AdvanceInferenceInfo): Bitmap? {
        if (frameBitmap == null) {
            return frameBitmap
        }

        if (info.imageRotation % 90 == 0) {
            info.frameSize = Size(frameBitmap.height, frameBitmap.width)
        } else {
            info.frameSize = Size(frameBitmap.width, frameBitmap.height)
        }

        val matrix = MatrixUtils.getTransformationMatrix(frameBitmap.width,
            frameBitmap.height, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE,
            info.imageRotation, true, fitIn = true)

        val preScaledBitmap = ImageUtils.createTransformedBitmap(frameBitmap,
            matrix, paddingColor = Color.BLACK)

        val flipped = if (info.cameraLensFacing == CameraSelector.LENS_FACING_FRONT) {
            ImageUtils.flipBitmap(preScaledBitmap)
        } else {
            preScaledBitmap
        }

        dumpIntermediateBitmap(flipped, PRE_SCALED_IMAGE_FILE)

        return flipped
    }

    override fun isDumpIntermediatesEnabled(): Boolean {
        return false
    }

}

class StyleTransferCameraFragment : AbsExampleCameraFragment<AdvanceInferenceInfo, StyleTransferResult>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StyleTransferPrefs.prefsChange.observe(this, Observer<PrefsChange> {
            if (it.prefKey == StyleTransferPrefs.KEY_SELECTED_STYLE) {
                val imageAnalyzer = analyzer

                if (imageAnalyzer is StyleTransferAnalyzer) {
                    imageAnalyzer.selectStyle(StyleTransferPrefs.getSelectedStyle(requireContext()))
                }
            }
        })
    }

    override fun createAnalyzer(screenAspectRatio: Int, rotation: Int, lensFacing: Int)
            : AbsImageAnalyzer<AdvanceInferenceInfo, StyleTransferResult> {
        return StyleTransferAnalyzer(rotation, lensFacing)
    }

}