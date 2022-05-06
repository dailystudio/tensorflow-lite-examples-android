package com.dailystudio.tflite.example.reinforcementlearning.viewmodel

import android.app.Application
import org.tensorflow.lite.examples.reinforcementlearning.BoardCell
import org.tensorflow.lite.examples.reinforcementlearning.BoardCellStatus
import org.tensorflow.lite.examples.reinforcementlearning.HiddenBoardCellStatus
import org.tensorflow.lite.examples.reinforcementlearning.PlayerBoardCellManager
import org.tensorflow.lite.examples.reinforcementlearning.model.AgentBoardCellViewModel
import org.tensorflow.lite.examples.reinforcementlearning.model.PlayerBoardCellViewModel

class PlayerBoardViewModel(application: Application): PlayerBoardCellViewModel(application) {

    fun agentActionOn(x: Int, y: Int) {
        val playerCellId = BoardCell.getIdByPos(x, y)
        val playerCell = PlayerBoardCellManager.get(playerCellId) ?: return

        if (playerCell.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
            // Hit
            playerCell.status = BoardCellStatus.HIT
//                agentHits++
//                agentHitsTextView?.setText("Player board:\n$agentHits hits")
        } else {
            // Miss
            playerCell.status = BoardCellStatus.MISS
        }

        updatePlayerBoardCell(playerCell)
    }

}