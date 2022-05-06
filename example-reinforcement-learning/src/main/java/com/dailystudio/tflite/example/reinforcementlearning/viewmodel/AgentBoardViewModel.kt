package com.dailystudio.tflite.example.reinforcementlearning.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.dailystudio.tflite.example.common.AbsTFLiteModelRunner
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.reinforcementlearning.*
import org.tensorflow.lite.examples.reinforcementlearning.model.AgentBoardCellViewModel
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

class AgentBoardViewModel(application: Application): AgentBoardCellViewModel(application) {

    private var analyzer: ReinforcementLearningAnalyzer? = null
    private var lock = Object()

    init {
    }

    suspend fun playerActionOn(x:Int, y: Int):Int {
        val agentCellId = BoardCell.getIdByPos(x, y)
        val agentCell = AgentBoardCellManager.get(agentCellId) ?: return -1

        if (agentCell.status == BoardCellStatus.UNTRIED) {
            agentCell.status = if (agentCell.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
                BoardCellStatus.HIT

//                playerHits++
//                playerHitsTextView?.setText("Agent board:\n$playerHits hits")
            } else {
                BoardCellStatus.MISS
            }
        }

        updateAgentBoardCell(agentCell)

        return predictNextMove()
    }

    private suspend fun predictNextMove(): Int {
        return withContext(Dispatchers.IO) {
            synchronized(lock) {
                if (analyzer == null) {
                    analyzer = ReinforcementLearningAnalyzer(false)
                }

                 analyzer?.run(PlayerBoardCellManager.dumpStatus(),
                    InferenceSettingsPrefs.instance) ?: -1
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        synchronized(lock) {
            analyzer?.destroyModel()
        }
    }


}