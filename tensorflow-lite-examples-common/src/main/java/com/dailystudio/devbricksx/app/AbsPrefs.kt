package com.dailystudio.devbricksx.app

import android.content.*
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData


data class PrefsChange(val appPrefs: AbsPrefs,
                       val prefKey: String)

abstract class AbsPrefs {

    val prefsChange: MutableLiveData<PrefsChange> = MutableLiveData()

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    private fun getEditor(context: Context): SharedPreferences.Editor {
        val sharedPref = getSharedPreferences(context)

        return sharedPref.edit()
    }

    fun setStringPrefValue(context: Context,
                           pref: String, sValue: String?) {
        val editor = getEditor(context)
        editor.putString(pref, sValue)
        editor.commit()

        notifyPrefChanged(context, pref)
    }

    fun setBooleanPrefValue(context: Context,
                            pref: String,
                            bValue: Boolean) {
        val editor = getEditor(context)
        editor.putBoolean(pref, bValue)
        editor.commit()

        notifyPrefChanged(context, pref)
    }

    fun setLongPrefValue(context: Context,
                         pref: String,
                         lValue: Long) {
        val editor = getEditor(context)
        editor.putLong(pref, lValue)
        editor.commit()

        notifyPrefChanged(context, pref)
    }

    fun setIntegerPrefValue(context: Context,
                            pref: String,
                            iValue: Int) {
        val editor = getEditor(context)
        editor.putInt(pref, iValue)
        editor.commit()

        notifyPrefChanged(context, pref)
    }

    fun setFloatPrefValue(context: Context,
                          pref: String,
                          fValue: Float) {
        val editor = getEditor(context)
        editor.putFloat(pref, fValue)
        editor.commit()

        notifyPrefChanged(context, pref)
    }

    fun getStringPrefValue(context: Context,
                           pref: String): String? {
        val sharedPref = getSharedPreferences(context)
        return sharedPref.getString(pref, null)
    }

    fun getBooleanPrefValue(context: Context,
                            pref: String): Boolean {
        return getBooleanPrefValue(context, pref, false)
    }

    fun getBooleanPrefValue(context: Context,
                            pref: String,
                            defVal: Boolean): Boolean {
        val sharedPref = getSharedPreferences(context)

        return sharedPref.getBoolean(pref, defVal)
    }

    fun getLongPrefValue(context: Context,
                         pref: String): Long {
        return getLongPrefValue(context, pref, 0L)
    }

    fun getLongPrefValue(context: Context,
                         pref: String,
                         defVal: Long): Long {
        val sharedPref = getSharedPreferences(context)

        return sharedPref.getLong(pref, defVal)
    }

    fun getIntegerPreValue(context: Context,
                           pref: String): Int {
        return getIntegerPreValue(context, pref, 0)
    }

    fun getIntegerPreValue(context: Context,
                           pref: String,
                           defVal: Int): Int {
        val sharedPref = getSharedPreferences(context) ?: return defVal

        return sharedPref.getInt(pref, defVal)
    }

    fun getFloatPreValue(context: Context,
                         pref: String): Float {
        return getFloatPreValue(context, pref, 0.0f)
    }

    fun getFloatPreValue(context: Context,
                         pref: String,
                         defVal: Float): Float {
        val sharedPref = getSharedPreferences(context)

        return sharedPref.getFloat(pref, defVal)
    }

    protected fun notifyPrefChanged(context: Context, key: String) {
        if (TextUtils.isEmpty(key)) {
            return
        }

        prefsChange.postValue(PrefsChange(this, key))
    }

    protected abstract val prefName: String

}
