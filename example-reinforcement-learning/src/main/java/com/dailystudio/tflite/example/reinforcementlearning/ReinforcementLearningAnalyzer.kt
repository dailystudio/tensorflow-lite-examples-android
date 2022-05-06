package com.dailystudio.tflite.example.reinforcementlearning

import android.content.Context
import com.dailystudio.tflite.example.common.AbsTFLiteModelRunner
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import org.tensorflow.lite.examples.reinforcementlearning.*
import org.tensorflow.lite.support.model.Model

class ReinforcementLearningAnalyzer(useAverageTime: Boolean)
    : AbsTFLiteModelRunner<PlaneStrikeAgent, Array<Array<BoardCellStatus>>, InferenceInfo, Int>(useAverageTime)
{
    override fun createModel(
        context: Context,
        device: Model.Device,
        numOfThreads: Int,
        settings: InferenceSettingsPrefs
    ): PlaneStrikeAgent? {
        return if (Constants.USE_MODEL_FROM_TF) {
            RLAgent(context, device, numOfThreads)
        } else {
            RLAgentFromTFAgents(context, device, numOfThreads)
        }
    }

    override fun analyze(
        model: PlaneStrikeAgent,
        data: Array<Array<BoardCellStatus>>,
        info: InferenceInfo
    ): Int? {
        return model.predictNextMove(data)
    }

    override fun createInferenceInfo(): InferenceInfo {
        return InferenceInfo()
    }
}
