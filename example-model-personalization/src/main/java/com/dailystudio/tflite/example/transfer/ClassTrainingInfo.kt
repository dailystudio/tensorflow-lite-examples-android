package com.dailystudio.tflite.example.transfer

import android.graphics.Bitmap
import com.dailystudio.devbricksx.annotations.data.InMemoryCompanion
import com.dailystudio.devbricksx.annotations.viewmodel.ViewModel
import com.dailystudio.devbricksx.inmemory.InMemoryObject

@InMemoryCompanion
@ViewModel
data class ClassTrainingInfo(val className: String): InMemoryObject<String> {
    var numOfSamples: Int = 0
    var lastSample: Bitmap? = null

    override fun toString(): String {
        return buildString {
            append("${this@ClassTrainingInfo.javaClass.simpleName}: ")
            append("class = ${className}, ")
            append("numOfSamples = ${numOfSamples}, ")
            append("lastSample = ${lastSample}")
        }
    }

    override fun getKey() = className

}