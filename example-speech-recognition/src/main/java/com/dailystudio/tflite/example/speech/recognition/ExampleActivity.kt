package com.dailystudio.tflite.example.speech.recognition

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.StringUtils
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.speech.recognition.fragment.CommandsListFragment
import com.dailystudio.tflite.example.speech.recognition.fragment.SpeechRecognitionFragment
import com.dailystudio.tflite.example.speech.recognition.model.CommandViewModel
import org.tensorflow.lite.examples.speech.RecognizeCommands

class ExampleActivity : AbsExampleActivity<AudioInferenceInfo, RecognizeCommands.RecognitionResult>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        generateCommands()

        val fragment = findFragment(R.id.fragment_commands)
        if (fragment is CommandsListFragment) {
            fragment.setRecyclerViewTouchEnabled(false)
        }
    }

    private fun generateCommands() {
        val viewModel = ViewModelProvider(this).get(
            CommandViewModel::class.java)

        val labels = StringUtils.linesFromAsset(this, "conv_actions_labels.txt")

        var id = 0
        for (label in labels) {
            Logger.debug("label: $label")
            if (label.startsWith("_")) {
                continue
            }

            val command = Command(id++, label)

            viewModel.insertCommand(command)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_speech_recognition
    }

    override fun createBaseFragment(): Fragment {
        return SpeechRecognitionFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: RecognizeCommands.RecognitionResult) {
        Logger.debug("command: ${results.foundCommand}")
        Logger.debug("score: ${results.score}")
        Logger.debug("isNew: ${results.isNewCommand}")

        val viewModel = ViewModelProvider(this).get(
            CommandViewModel::class.java)

        val commands = viewModel.getCommands()
        for (command in commands) {
            if (results.foundCommand == command.label
                && results.score > 0.5) {
                command.prop = results.score
            } else {
                command.prop = 0f
            }

            viewModel.updateCommand(command)
        }
    }

    override fun getExampleName(): CharSequence? {
        return getString(R.string.app_name)
    }

    override fun getExampleIconResource(): Int {
        return R.drawable.about_icon
    }

    override fun getExampleDesc(): CharSequence? {
        return getString(R.string.app_desc)
    }

}
