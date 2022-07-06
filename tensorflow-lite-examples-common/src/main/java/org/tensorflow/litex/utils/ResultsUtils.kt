package org.tensorflow.litex.utils

object ResultsUtils {

    fun safeToPrintableLog(`object`: Any?): String {
        return `object`?.let {
            it.toString().replace("%", "%%")
        } ?: ""
    }

}