package com.dailystudio.tflite.example.common.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ImageUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun ImageUtils.scaleBitmapWithRatio(bitmap: Bitmap?,
                                    destWidth: Int,
                                    destHeight: Int,
                                    reduceLoss: Boolean = true): Bitmap? {
    if (bitmap == null) {
        return bitmap
    }

    val srcInPortrait = (bitmap.width <= bitmap.height)
    val destInPortrait = (destWidth <= destHeight)

    val dw = if (srcInPortrait == destInPortrait) {
        destWidth
    } else {
        destHeight
    }.toFloat()

    val dh = if (srcInPortrait == destInPortrait) {
        destHeight
    } else {
        destWidth
    }.toFloat()

    val shrink = if (dw > dh) {
        dh < bitmap.height
    } else {
        dw < bitmap.width
    }

    val wRatio = dw / bitmap.width
    val hRatio = dh / bitmap.height

    val ratio = if (reduceLoss) {
        if (shrink) {
            max(wRatio, hRatio)
        } else {
            min(wRatio, hRatio)
        }
    } else {
        if (shrink) {
            min(wRatio, hRatio)
        } else {
            max(wRatio, hRatio)
        }
    }

    return scaleBitmap(bitmap,
        (bitmap.width * ratio).roundToInt(),
        (bitmap.height * ratio).roundToInt())
}

fun ImageUtils.scaleAndCenterCrop(bitmap: Bitmap,
                                  dstWidth: Int, dstHeight: Int): Bitmap {
    val srcWidth = bitmap.width
    val srcHeight = bitmap.height

    val matrix = getCropMatrix(srcWidth, srcHeight, dstWidth, dstHeight)

    val scaledBitmap = if (srcWidth > srcHeight) {
        Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(dstHeight, dstWidth, Bitmap.Config.ARGB_8888)
    }

    val canvas = Canvas(scaledBitmap)
    canvas.drawBitmap(bitmap, matrix, null)

    return scaledBitmap
}

fun ImageUtils.getCropMatrix(srcWidth: Int, srcHeight: Int,
                             dstWidth: Int, dstHeight: Int): Matrix {
    Logger.debug("srcWidth = $srcWidth, dstWidth = $dstWidth")
    val matrix = Matrix()

    val transpose = (srcHeight > srcWidth)
    val outWidth = if (transpose) dstHeight else dstWidth
    val outHeight = if (transpose) dstWidth else dstHeight
    Logger.debug("outWidth = $outWidth, outHeight = $outHeight")

    if (srcWidth != dstWidth || srcHeight != dstHeight) {
        val scaleFactorX = outWidth / srcWidth.toFloat()
        val scaleFactorY = outHeight / srcHeight.toFloat()
        Logger.debug("scaleFactorX = $scaleFactorX, scaleFactorY = $scaleFactorY")

        val scaleFactor = max(scaleFactorX, scaleFactorY)
        matrix.postScale(scaleFactor, scaleFactor)

        val scaleWidth = srcWidth * scaleFactor
        val scaleHeight = srcHeight * scaleFactor
        Logger.debug("scaleWidth = $scaleWidth, scaleHeight = $scaleHeight")

        val translateX = (outWidth - scaleWidth) / 2.0f
        val translateY = (outHeight - scaleHeight) / 2.0f
        Logger.debug("translateX = $translateX, translateY = $translateY")
        matrix.postTranslate(translateX, translateY)

    }

    return matrix
}

/**
 * Returns a transformation matrix from one reference frame into another. Handles cropping (if
 * maintaining aspect ratio is desired) and rotation.
 *
 * @param srcWidth Width of source frame.
 * @param srcHeight Height of source frame.
 * @param dstWidth Width of destination frame.
 * @param dstHeight Height of destination frame.
 * @param applyRotation Amount of rotation to apply from one frame to another. Must be a multiple
 * of 90.
 * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
 * cropping the image if necessary.
 * @return The transformation fulfilling the desired requirements.
 */
fun ImageUtils.getTransformationMatrix(srcWidth: Int, srcHeight: Int,
                                       dstWidth: Int, dstHeight: Int,
                                       applyRotation: Int,
                                       maintainAspectRatio: Boolean): Matrix {
    val matrix = Matrix()
    if (applyRotation != 0) {
        if (applyRotation % 90 != 0) {
            Logger.warn("Rotation of %d % 90 != 0", applyRotation)
        }

        // Translate so center of image is at origin.
        matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

        // Rotate around origin.
        matrix.postRotate(applyRotation.toFloat())
    }

    // Account for the already applied rotation, if any, and then determine how
    // much scaling is needed for each axis.
    val transpose = (abs(applyRotation) + 90) % 180 == 0
    val inWidth = if (transpose) srcHeight else srcWidth
    val inHeight = if (transpose) srcWidth else srcHeight

    // Apply scaling if necessary.
    if (inWidth != dstWidth || inHeight != dstHeight) {
        val scaleFactorX = dstWidth / inWidth.toFloat()
        val scaleFactorY = dstHeight / inHeight.toFloat()
        if (maintainAspectRatio) {
            // Scale by minimum factor so that dst is filled completely while
            // maintaining the aspect ratio. Some image may fall off the edge.
            val scaleFactor = max(scaleFactorX, scaleFactorY)
            matrix.postScale(scaleFactor, scaleFactor)
        } else {
            // Scale exactly to fill dst from src.
            matrix.postScale(scaleFactorX, scaleFactorY)
        }
    }

    if (applyRotation != 0) {
        // Translate back from origin centered reference to destination frame.
        matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
    }

    return matrix
}
