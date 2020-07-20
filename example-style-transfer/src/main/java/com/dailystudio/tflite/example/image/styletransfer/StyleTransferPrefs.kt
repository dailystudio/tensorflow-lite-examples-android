package com.dailystudio.tflite.example.image.styletransfer

import android.content.Context
import com.dailystudio.devbricksx.preference.AbsPrefs

object StyleTransferPrefs : AbsPrefs() {

    const val KEY_SELECTED_STYLE = "selected-style"
    const val DEFAULT_SELECTION = "style0.jpg"

    override val prefName: String
        get() = "style-transfer-prefs"

    fun setSelectedStyle(context: Context,
                         name: String) {
        setStringPrefValue(context, KEY_SELECTED_STYLE, name)
    }

    fun getSelectedStyle(context: Context): String {
        return getStringPrefValue(context, KEY_SELECTED_STYLE) ?: DEFAULT_SELECTION
    }

}