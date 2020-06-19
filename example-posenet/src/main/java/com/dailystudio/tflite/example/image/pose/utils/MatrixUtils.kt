package com.dailystudio.tflite.example.image.pose.utils

import android.graphics.Matrix
import org.tensorflow.lite.examples.posenet.lib.KeyPoint
import org.tensorflow.lite.examples.posenet.lib.Position
import kotlin.math.roundToInt

fun Matrix.mapPosition(p: Position) {
    val pts = floatArrayOf(
        p.x.toFloat(),
        p.y.toFloat())
    mapPoints(pts)

    p.x = pts[0].roundToInt()
    p.y = pts[1].roundToInt()
}

fun Matrix.mapKeyPoint(kp: KeyPoint) {
    val pts = floatArrayOf(
        kp.position.x.toFloat(),
        kp.position.y.toFloat())
    mapPoints(pts)

    kp.position.x = pts[0].roundToInt()
    kp.position.y = pts[1].roundToInt()
}