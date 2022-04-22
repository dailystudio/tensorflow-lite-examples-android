package com.dailystudio.tflite.example.image.classification.fragment

import android.content.Context
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.settings.AbsSetting
import com.dailystudio.devbricksx.settings.RadioSetting
import com.dailystudio.devbricksx.settings.SimpleRadioSettingItem
import com.dailystudio.devbricksx.settings.SwitchSetting
import com.dailystudio.tflite.example.common.ui.InferenceSettingsFragment
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.image.classification.ImageClassificationSettingsPrefs
import com.dailystudio.tflite.example.image.classification.R
import org.tensorflow.lite.examples.classification.tflite.Classifier

class ImageClassificationSettingsFragment : InferenceSettingsFragment() {

    override fun createSettings(context: Context): Array<AbsSetting> {
        val settings: MutableList<AbsSetting> =
            super.createSettings(context).toMutableList()

        val settingsPrefs = getInferenceSettingsPrefs()
        if (settingsPrefs !is ImageClassificationSettingsPrefs) {
            return settings.toTypedArray()
        }


        val enableImagePreprocess = object: SwitchSetting(
            context,
            ImageClassificationSettingsPrefs.PREF_ENABLE_IMAGE_PRE_SCALE,
            R.drawable.ic_setting_image_pre_scale,
            R.string.setting_image_pre_scale,
        ) {

            override fun isOn(): Boolean {
                Logger.debug("[${settingsPrefs.hashCode()}]GET PROP: PRE: ${settingsPrefs.enableImagePreScale}")

                return settingsPrefs.enableImagePreScale
            }

            override fun setOn(on: Boolean) {
                Exception("isOn").printStackTrace()

                settingsPrefs.enableImagePreScale = on
                Logger.debug("[${settingsPrefs.hashCode()}]SET PROP: PRE: ${settingsPrefs.enableImagePreScale}")

            }

        }

        val models = arrayOf(
            SimpleRadioSettingItem(context,
                Classifier.Model.QUANTIZED_MOBILENET.toString(),
                R.string.model_quantized_mobile_net),
            SimpleRadioSettingItem(context,
                Classifier.Model.FLOAT_MOBILENET.toString(),
                R.string.model_float_mobile_net),
            SimpleRadioSettingItem(context,
                Classifier.Model.QUANTIZED_EFFICIENTNET.toString(),
                R.string.model_quantized_efficient_net),
            SimpleRadioSettingItem(context,
                Classifier.Model.FLOAT_EFFICIENTNET.toString(),
                R.string.model_float_efficient_net)
        )

        val modelSetting = object: RadioSetting<SimpleRadioSettingItem>(
            context,
            ImageClassificationSettingsPrefs.PREF_TF_LITE_MODEL,
            R.drawable.ic_setting_models,
            R.string.setting_model,
            models) {
            override val selectedId: String?
                get() = settingsPrefs.tfLiteModel

            override fun setSelected(selectedId: String?) {
                settingsPrefs.tfLiteModel = selectedId
            }
        }

        settings.add(modelSetting)
        settings.add(enableImagePreprocess)

        return settings.toTypedArray()
    }

    override fun getInferenceSettingsPrefs(): InferenceSettingsPrefs {
        return ImageClassificationSettingsPrefs.instance
    }

}