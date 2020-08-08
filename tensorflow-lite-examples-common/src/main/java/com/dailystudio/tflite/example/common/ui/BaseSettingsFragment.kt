package com.dailystudio.tflite.example.common.ui

import android.content.Context
import com.dailystudio.devbricksx.settings.*
import com.dailystudio.tflite.example.common.R
import org.tensorflow.lite.support.model.Model

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

        return arrayOf(deviceSetting)
    }

}