package com.dailystudio.tflite.example.video.classification.fragment

import android.content.Context
import com.dailystudio.devbricksx.settings.AbsSetting
import com.dailystudio.devbricksx.settings.RadioSetting
import com.dailystudio.devbricksx.settings.SimpleRadioSettingItem
import com.dailystudio.tensorflow.litex.fragment.InferenceSettingsFragment
import com.dailystudio.tflite.example.video.classification.R
import com.dailystudio.tflite.example.video.classification.VideoClassificationSettingsPrefs
import org.tensorflow.lite.examples.videoclassification.ml.VideoClassifier
import com.dailystudio.tensorflow.litex.ui.InferenceSettingsPrefs

class VideoClassificationSettingsFragment : InferenceSettingsFragment() {

    override fun createSettings(context: Context): Array<AbsSetting> {
        val settings: MutableList<AbsSetting> =
            super.createSettings(context).toMutableList()

        val settingsPrefs = getInferenceSettingsPrefs()
        if (settingsPrefs !is VideoClassificationSettingsPrefs) {
            return settings.toTypedArray()
        }

        val models = arrayOf(
            SimpleRadioSettingItem(context,
                VideoClassifier.ClassifierModel.MOVINET_A0.toString(),
                R.string.model_movinet_a0),
            SimpleRadioSettingItem(context,
                VideoClassifier.ClassifierModel.MOVINET_A1.toString(),
                R.string.model_movinet_a1),
            SimpleRadioSettingItem(context,
                VideoClassifier.ClassifierModel.MOVINET_A2.toString(),
                R.string.model_movinet_a2),
        )

        val modelSetting = object: RadioSetting<SimpleRadioSettingItem>(
            context,
            VideoClassificationSettingsPrefs.PREF_CLASSIFIER_MODEL,
            R.drawable.ic_setting_models,
            R.string.setting_model,
            models) {
            override val selectedId: String?
                get() = settingsPrefs.classifierModel

            override fun setSelected(selectedId: String?) {
                settingsPrefs.classifierModel = selectedId ?: VideoClassifier.ClassifierModel.MOVINET_A0.toString()
            }
        }

        settings.add(modelSetting)

        return settings.toTypedArray()
    }

    override fun getInferenceSettingsPrefs(): InferenceSettingsPrefs {
        return VideoClassificationSettingsPrefs.instance
    }

}