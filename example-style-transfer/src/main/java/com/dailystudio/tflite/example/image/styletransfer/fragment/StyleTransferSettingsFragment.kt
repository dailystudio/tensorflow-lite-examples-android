package com.dailystudio.tflite.example.image.styletransfer.fragment

import android.content.Context
import com.dailystudio.devbricksx.settings.AbsSetting
import com.dailystudio.devbricksx.settings.RadioSetting
import com.dailystudio.devbricksx.settings.SimpleRadioSettingItem
import com.dailystudio.devbricksx.settings.SwitchSetting
import org.tensorflow.litex.fragment.InferenceSettingsFragment
import com.dailystudio.tflite.example.image.styletransfer.R
import com.dailystudio.tflite.example.image.styletransfer.StyleTransferSettingsPrefs
import org.tensorflow.lite.examples.styletransfer.FSTModel
import org.tensorflow.litex.ui.InferenceSettingsPrefs

class StyleTransferSettingsFragment : InferenceSettingsFragment() {

    override fun createSettings(context: Context): Array<AbsSetting> {
        val settings: MutableList<AbsSetting> =
            super.createSettings(context).toMutableList()

        val settingsPrefs = getInferenceSettingsPrefs()
        if (settingsPrefs !is StyleTransferSettingsPrefs) {
            return settings.toTypedArray()
        }

        val models = arrayOf(
            SimpleRadioSettingItem(context,
                FSTModel.FastStyleTransferInt8.toString(),
                R.string.model_fst_int8),
            SimpleRadioSettingItem(context,
                FSTModel.FastStyleTransferFloat16.toString(),
                R.string.model_fst_float16),
        )

        val modelSetting = object: RadioSetting<SimpleRadioSettingItem>(
            context,
            StyleTransferSettingsPrefs.PREF_TF_LITE_MODEL,
            R.drawable.ic_setting_models,
            R.string.setting_model,
            models) {
            override val selectedId: String?
                get() = settingsPrefs.tfLiteModel

            override fun setSelected(selectedId: String?) {
                settingsPrefs.tfLiteModel = selectedId ?: FSTModel.FastStyleTransferInt8.toString()
            }
        }

        val reusePredict = object: SwitchSetting(
            context,
            StyleTransferSettingsPrefs.PREF_REUSE_PREDICT,
            R.drawable.ic_setting_reuse,
            R.string.setting_reuse_predict,
        ) {
            override fun isOn(): Boolean {
                return settingsPrefs.reusePredict
            }

            override fun setOn(on: Boolean) {
                settingsPrefs.reusePredict = on
            }

        }

        settings.add(reusePredict)
        settings.add(modelSetting)

        return settings.toTypedArray()
    }

    override fun getInferenceSettingsPrefs(): InferenceSettingsPrefs {
        return StyleTransferSettingsPrefs.instance
    }

}