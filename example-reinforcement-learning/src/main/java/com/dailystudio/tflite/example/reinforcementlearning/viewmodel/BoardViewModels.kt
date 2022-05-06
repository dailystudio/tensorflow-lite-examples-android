package com.dailystudio.tflite.example.reinforcementlearning.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dailystudio.tflite.example.common.ui.InferenceSettingsPrefs
import com.dailystudio.tflite.example.reinforcementlearning.ReinforcementLearningAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.reinforcementlearning.*
import org.tensorflow.lite.examples.reinforcementlearning.model.AgentBoardCellViewModel
import org.tensorflow.lite.examples.reinforcementlearning.model.PlayerBoardCellViewModel


class BoardHits {

    private val _hits = MutableLiveData(0)
    val value: LiveData<Int> = _hits

    fun getHits(): Int? {
        return _hits.value
    }

    fun markHit() {
        val oldValue = _hits.value ?: 0
        _hits.postValue(oldValue + 1)
    }

    fun reset() {
        _hits.postValue(0)
    }

}

class AgentBoardViewModel(application: Application): AgentBoardCellViewModel(application) {

    val hits = BoardHits()

    private var analyzer: ReinforcementLearningAnalyzer? = null
    private var lock = Object()

    suspend fun playerActionOn(x:Int, y: Int):Int {
        val agentCellId = BoardCell.getIdByPos(x, y)
        val agentCell = AgentBoardCellManager.get(agentCellId) ?: return -1

        if (agentCell.status == BoardCellStatus.UNTRIED) {
            agentCell.status = if (agentCell.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
                hits.markHit()

                BoardCellStatus.HIT
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

class PlayerBoardViewModel(application: Application): PlayerBoardCellViewModel(application) {

    val hits = BoardHits()

    val _lastAction = MutableLiveData(PlayerBoardCell(0, 0))
    val lastAction = _lastAction

    fun agentActionOn(x: Int, y: Int) {
        val playerCellId = BoardCell.getIdByPos(x, y)
        val playerCell = PlayerBoardCellManager.get(playerCellId) ?: return

        playerCell.status = if (playerCell.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
            // Hit
            hits.markHit()
            BoardCellStatus.HIT
        } else {
            // Miss
            BoardCellStatus.MISS
        }

        updatePlayerBoardCell(playerCell)
        lastAction.postValue(playerCell)
    }

}