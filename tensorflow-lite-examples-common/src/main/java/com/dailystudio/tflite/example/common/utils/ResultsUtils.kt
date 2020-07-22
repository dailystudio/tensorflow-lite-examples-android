package com.dailystudio.tflite.example.common.utils

object ResultsUtils {

    fun safeToPrintableLog(`object`: Any?): String {
        return `object`?.let {
            it.toString().replace("%", "%%")
        } ?: ""
    }

}