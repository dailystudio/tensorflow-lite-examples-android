package com.dailystudio.tflite.example.common.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.settings.*
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.R
import org.tensorflow.lite.support.model.Model
import kotlin.math.roundToInt

open class InferenceSettingsFragment: AbsSettingsDialogFragment() {

    protected open val allowSkipPreprocess = false

    override fun createSettings(context: Context): Array<AbsSetting> {
        val settingsPrefs = getInferenceSettingsPrefs()

        val useAverageTime = object: SwitchSetting(
            context,
            InferenceSettingsPrefs.PREF_USER_AVERAGE_TIME,
            R.drawable.ic_setting_use_avg_time,
            R.string.setting_use_average,
        ) {

            override fun isOn(): Boolean {
                Logger.debug("[${settingsPrefs.hashCode()}] GET PROP: AVG: ${settingsPrefs.userAverageTime}")
                return settingsPrefs.userAverageTime
            }

            override fun setOn(on: Boolean) {
                settingsPrefs.userAverageTime = on
                Logger.debug("[${settingsPrefs.hashCode()}]SET PROP: AVG: ${settingsPrefs.userAverageTime}")
            }

        }

        val enableImagePreprocess = object: SwitchSetting(
            context,
            InferenceSettingsPrefs.PREF_ENABLE_IMAGE_PREPROCESS,
            R.drawable.ic_setting_pre_process,
            R.string.setting_pre_process,
        ) {

            override fun isOn(): Boolean {
                Logger.debug("[${settingsPrefs.hashCode()}]GET PROP: PRE: ${settingsPrefs.enableImagePreprocess}")

                return settingsPrefs.enableImagePreprocess
            }

            override fun setOn(on: Boolean) {
                Exception("isOn").printStackTrace()

                settingsPrefs.enableImagePreprocess = on
                Logger.debug("[${settingsPrefs.hashCode()}]SET PROP: PRE: ${settingsPrefs.enableImagePreprocess}")

            }

        }

        val devices = arrayOf(
            SimpleRadioSettingItem(context,
                Model.Device.CPU.toString(), R.string.label_cpu),
            SimpleRadioSettingItem(context,
                Model.Device.GPU.toString(), R.string.label_gpu),
            SimpleRadioSettingItem(context,
                Model.Device.NNAPI.toString(), R.string.label_nnapi)
        )

        val deviceSetting = object: RadioSetting<SimpleRadioSettingItem>(
            context,
            InferenceSettingsPrefs.PREF_DEVICE,
            R.drawable.ic_setting_device,
            R.string.setting_device,
            devices) {
            override val selectedId: String?
                get() = settingsPrefs.device

            override fun setSelected(selectedId: String?) {
                selectedId?.let {
                    settingsPrefs.device = it
                }
            }
        }


        val threadSetting = object: SeekBarSetting(
            context,
            InferenceSettingsPrefs.PREF_NUMBER_OF_THREADS,
            R.drawable.ic_setting_threads,
            R.string.setting_threads) {
            override fun getMaxValue(context: Context): Float {
                return InferenceSettings.MAX_NUM_OF_THREADS.toFloat()
            }

            override fun getMinValue(context: Context): Float {
                return InferenceSettings.MIN_NUM_OF_THREADS.toFloat()
            }

            override fun getProgress(context: Context): Float {
                return settingsPrefs.numberOfThreads.toFloat()
            }

            override fun getStep(context: Context): Float {
                return InferenceSettings.NUM_OF_THREADS_STEP.toFloat()
            }

            override fun setProgress(context: Context, progress: Float) {
                settingsPrefs.numberOfThreads = progress.roundToInt()
            }

        }

        val settings = mutableListOf(
            useAverageTime,
            deviceSetting,
            threadSetting,
        ).apply {
            if (allowSkipPreprocess) {
                add(0, enableImagePreprocess)
            }
        }

        return settings.toTypedArray()
    }

    override fun getDialogThumbImageDrawable(): Drawable? {
        return ResourcesCompatUtils.getDrawable(requireContext(), R.drawable.settings_top)
    }

    open fun getInferenceSettingsPrefs(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs.instance
    }

}