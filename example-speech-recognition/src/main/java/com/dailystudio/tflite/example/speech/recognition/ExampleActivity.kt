package com.dailystudio.tflite.example.speech.recognition

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.FileUtils
import com.dailystudio.devbricksx.utils.StringUtils
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.speech.recognition.fragment.CommandsListFragment
import com.dailystudio.tflite.example.speech.recognition.fragment.SpeechRecognitionFragment
import com.dailystudio.tflite.example.speech.recognition.model.CommandViewModel
import kotlinx.android.synthetic.main.activity_example_speech_recognition.*
import kotlinx.android.synthetic.main.fragment_commands_list.*
import org.tensorflow.lite.examples.speech.RecognizeCommands
import org.tensorflow.lite.support.common.FileUtil
import java.io.StringReader

class ExampleActivity : AbsExampleActivity<AudioInferenceInfo, RecognizeCommands.RecognitionResult>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        generateCommands()

        (fragment_commands as CommandsListFragment).setRecyclerViewTouchEnabled(false)
    }

    private fun generateCommands() {
        val viewModel = ViewModelProvider(this).get(
            CommandViewModel::class.java)

        val labels = StringUtils.linesFromAsset(this, "conv_actions_labels.txt")
        for (label in labels) {
            Logger.debug("label: $label")
            if (label.startsWith("_")) {
                continue
            }

            val command = Command(label)

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

    override fun createSettingsView(): View? {
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

}
