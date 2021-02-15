package com.dailystudio.tflite.example.common.ui

import android.content.Context
import android.graphics.drawable.Drawable
import com.dailystudio.devbricksx.settings.*
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.R
import org.tensorflow.lite.support.model.Model
import kotlin.math.roundToInt

open class InferenceSettingsFragment: AbsSettingsDialogFragment() {

    override fun createSettings(context: Context): Array<AbsSetting> {
        val settingsPrefs = getInferenceSettingsPrefs()

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

        return arrayOf(deviceSetting, threadSetting)
    }

    override fun getDialogThumbImageDrawable(): Drawable? {
        return ResourcesCompatUtils.getDrawable(requireContext(), R.drawable.settings_top)
    }

    open fun getInferenceSettingsPrefs(): InferenceSettingsPrefs {
        return InferenceSettingsPrefs()
    }

}