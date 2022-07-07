package com.dailystudio.tflite.example.reinforcementlearning.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dailystudio.devbricksx.development.Logger
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.reinforcementlearning.*
import java.util.*

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


enum class GameState {
    Started,
    PlayerWin,
    AgentWin,
    DrawGame
}


class BoardViewModel(application: Application): AndroidViewModel(application) {

    val agentBoardHits = BoardHits()
    val playerBoardHits = BoardHits()

    private val _gameState = MutableLiveData(GameState.Started)
    val gameState = _gameState

    fun playerActionOn(x:Int, y: Int) {
        val agentCellId = BoardCell.getIdByPos(x, y)
        val agentCell = AgentBoardCellManager.get(agentCellId) ?: return

        if (agentCell.status == BoardCellStatus.UNTRIED) {
            agentCell.status = if (agentCell.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
                agentBoardHits.markHit()

                BoardCellStatus.HIT
            } else {
                BoardCellStatus.MISS
            }
        }

        AgentBoardCellManager.add(agentCell)
    }

    fun agentActionOn(x: Int, y: Int) {
        val playerCellId = BoardCell.getIdByPos(x, y)
        val playerCell = PlayerBoardCellManager.get(playerCellId) ?: return

        playerCell.status = if (playerCell.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
            // Hit
            playerBoardHits.markHit()
            BoardCellStatus.HIT
        } else {
            // Miss
            BoardCellStatus.MISS
        }

        PlayerBoardCellManager.add(playerCell)

        viewModelScope.launch {
            checkOrEndGame()
        }
    }

    fun isGameEnded(): Boolean {
        return _gameState.value != GameState.Started
    }

    fun resetGame() {
        _gameState.postValue(GameState.Started)

        initBoards()
        agentBoardHits.reset()
        playerBoardHits.reset()
    }

    private fun endGameWithState(endState: GameState) {
        _gameState.postValue(endState)
    }

    private fun checkOrEndGame() {
        if (isGameEnded()) {
            return
        }

        val agentHits = playerBoardHits.getHits() ?: 0
        val playerHits = agentBoardHits.getHits() ?: 0
        Logger.debug("Agent HITS: $agentHits, Player HITS: $playerHits")

        if (agentHits == Constants.PLANE_CELL_COUNT || playerHits == Constants.PLANE_CELL_COUNT) {

            val endState = if (agentHits == Constants.PLANE_CELL_COUNT && playerHits == Constants.PLANE_CELL_COUNT) {
                GameState.DrawGame
            } else if (agentHits == Constants.PLANE_CELL_COUNT) {
                GameState.AgentWin
            } else {
                GameState.PlayerWin
            }

            endGameWithState(endState)
        }
    }

    private fun initBoards() {
        val playerHiddenStatus =  Array(Constants.BOARD_SIZE) {
            Array(Constants.BOARD_SIZE) { HiddenBoardCellStatus.UNOCCUPIED }
        }

        val agentHiddenStatus =  Array(Constants.BOARD_SIZE) {
            Array(Constants.BOARD_SIZE) { HiddenBoardCellStatus.UNOCCUPIED }
        }

        placePlaneOnHiddenBoard(playerHiddenStatus)
        placePlaneOnHiddenBoard(agentHiddenStatus)

//        Logger.debug("PLAYER BOARD: $playerHiddenStatus")
//        Logger.debug("AGENT BOARD: $agentHiddenStatus")

        AgentBoardCellManager.clear()
        PlayerBoardCellManager.clear()
        for (y in 0 until Constants.BOARD_SIZE) {
            for (x in 0 until Constants.BOARD_SIZE) {
                AgentBoardCellManager.add(
                    AgentBoardCell(x, y).apply {
                        status = BoardCellStatus.UNTRIED
                        hiddenStatus = agentHiddenStatus[x][y]
                    }
                )

                PlayerBoardCellManager.add(
                    PlayerBoardCell(x, y).apply {
                        status = BoardCellStatus.UNTRIED
                        hiddenStatus = playerHiddenStatus[x][y]
                    }
                )
            }
        }
    }

    private fun placePlaneOnHiddenBoard(hiddenBoard: Array<Array<HiddenBoardCellStatus>>) {
        // Place the plane on the board
        // First, decide the plane's orientation
        //   0: heading right
        //   1: heading up
        //   2: heading left
        //   3: heading down
        val rand = Random()
        val planeOrientation = rand.nextInt(4)

        // Next, figure out the location of plane core as the '*' below
        //   | |      |      | |    ---
        //   |-*-    -*-    -*-|     |
        //   | |      |      | |    -*-
        //           ---             |
        val planeCoreX: Int
        val planeCoreY: Int
        when (planeOrientation) {
            0 -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 3) + 2
                // Populate the tail
                hiddenBoard[planeCoreX][planeCoreY - 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 1][planeCoreY - 2] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 1][planeCoreY - 2] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
            1 -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 3) + 1
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                // Populate the tail
                hiddenBoard[planeCoreX + 2][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 2][planeCoreY + 1] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 2][planeCoreY - 1] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
            2 -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 3) + 1
                // Populate the tail
                hiddenBoard[planeCoreX][planeCoreY + 2] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 1][planeCoreY + 2] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX + 1][planeCoreY + 2] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
            else -> {
                planeCoreX = rand.nextInt(Constants.BOARD_SIZE - 3) + 2
                planeCoreY = rand.nextInt(Constants.BOARD_SIZE - 2) + 1
                // Populate the tail
                hiddenBoard[planeCoreX - 2][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 2][planeCoreY + 1] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                hiddenBoard[planeCoreX - 2][planeCoreY - 1] =
                    HiddenBoardCellStatus.OCCUPIED_BY_PLANE
            }
        }

        // Finally, populate the 'cross' in the plane
        hiddenBoard[planeCoreX][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX + 1][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX - 1][planeCoreY] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX][planeCoreY + 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
        hiddenBoard[planeCoreX][planeCoreY - 1] = HiddenBoardCellStatus.OCCUPIED_BY_PLANE
    }
}