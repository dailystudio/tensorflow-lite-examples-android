package com.dailystudio.tflite.example.image.pose.utils

import android.graphics.Matrix
import org.tensorflow.lite.examples.posenet.lib.Position
import kotlin.math.roundToInt

fun Matrix.mapPosition(p: Position) {
    var pts = floatArrayOf(
        p.x.toFloat(),
        p.y.toFloat())
    mapPoints(pts)

    p.x = pts[0].roundToInt()
    p.y = pts[1].roundToInt()
}