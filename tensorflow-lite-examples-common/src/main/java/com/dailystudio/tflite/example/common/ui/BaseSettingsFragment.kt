package com.dailystudio.tflite.example.common.ui

import android.content.Context
import android.graphics.drawable.Drawable
import com.dailystudio.devbricksx.settings.*
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.tflite.example.common.R
import org.tensorflow.lite.support.model.Model
import kotlin.math.roundToInt

open class BaseSettingsFragment : AbsSettingsDialogFragment() {

    override fun createSettings(context: Context): Array<AbsSetting> {

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
            BaseSettingPrefs.PREF_DEVICE,
            R.drawable.ic_setting_device,
            R.string.setting_device,
            devices) {
            override val selectedId: String?
                get() = BaseSettingPrefs.device

            override fun setSelected(selectedId: String?) {
                selectedId?.let {
                    BaseSettingPrefs.device = it
                }
            }
        }


        val threadSetting = object: SeekBarSetting(
            context,
            BaseSettingPrefs.PREF_NUMBER_OF_THREADS,
            R.drawable.ic_setting_threads,
            R.string.setting_threads) {
            override fun getMaxValue(context: Context): Float {
                return BaseSetting.MAX_NUM_OF_THREADS.toFloat()
            }

            override fun getMinValue(context: Context): Float {
                return BaseSetting.MIN_NUM_OF_THREADS.toFloat()
            }

            override fun getProgress(context: Context): Float {
                return BaseSettingPrefs.numberOfThreads.toFloat()
            }

            override fun getStep(context: Context): Float {
                return BaseSetting.NUM_OF_THREADS_STEP.toFloat()
            }

            override fun setProgress(context: Context, progress: Float) {
                BaseSettingPrefs.numberOfThreads = progress.roundToInt()
            }

        }

        return arrayOf(deviceSetting, threadSetting)
    }

    override fun getSettingsTopImageDrawable(): Drawable? {
        return ResourcesCompatUtils.getDrawable(requireContext(), R.drawable.settings_top)
    }

}