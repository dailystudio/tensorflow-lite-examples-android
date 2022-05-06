package com.dailystudio.tflite.example.reinforcementlearning.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.DevBricksFragment
import com.dailystudio.tflite.example.reinforcementlearning.R
import com.dailystudio.tflite.example.reinforcementlearning.viewmodel.AgentBoardViewModel
import com.dailystudio.tflite.example.reinforcementlearning.viewmodel.PlayerBoardViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.reinforcementlearning.*
import java.util.*

class ReinforcementLearningFragment: DevBricksFragment() {

    companion object {
        const val AUTO_RESET_DELAY = 2000L
    }

    private lateinit var agentBoardViewModel: AgentBoardViewModel
    private lateinit var playerBoardViewModel: PlayerBoardViewModel

    private var resetJob: Job? = null
    private var gameEnded: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        agentBoardViewModel = ViewModelProvider(requireActivity())
            .get(AgentBoardViewModel::class.java)
        agentBoardViewModel.hits.value.observe(this) {
            Logger.debug("Agent Board HITS: $it")
//            playerHitsTextView?.text = "Agent board:\n$it hits"
        }

        playerBoardViewModel = ViewModelProvider(requireActivity())
            .get(PlayerBoardViewModel::class.java)
        playerBoardViewModel.hits.value.observe(this) {
            Logger.debug("Player Board HITS: $it")
//            agentHitsTextView?.text = "Player board:\n$it hits"
        }
        playerBoardViewModel.lastAction.observe(this) {
            checkOrEndGame()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playboard, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
    }

    private fun setupViews(fragmentView: View) {
        initGame()
    }

    private fun initGame() {
        initBoards()

        agentBoardViewModel.hits.reset()
        playerBoardViewModel.hits.reset()

        gameEnded = false

        val fragment = findChildFragment(R.id.fragment_agent_board)
        if (fragment is AgentBoardCellsFragment) {
            fragment.clickEnabled(true)
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

    private fun checkOrEndGame() {
        if (gameEnded) {
            return
        }

        val agentHits = playerBoardViewModel.hits.getHits() ?: 0
        val playerHits = agentBoardViewModel.hits.getHits() ?: 0
        Logger.debug("Agent HITS: $agentHits, Player HITS: $playerHits")

        if (agentHits == Constants.PLANE_CELL_COUNT || playerHits == Constants.PLANE_CELL_COUNT) {
            endGame()

            // Game ends
            val gameEndMessage: String
            gameEndMessage = if (agentHits == Constants.PLANE_CELL_COUNT
                && playerHits == Constants.PLANE_CELL_COUNT
            ) {
                "Draw game!"
            } else if (agentHits == Constants.PLANE_CELL_COUNT) {
                "Agent wins!"
            } else {
                "You win!"
            }
            Toast.makeText(context, gameEndMessage, Toast.LENGTH_LONG).show()
            // Automatically reset game UI after 2 seconds

            resetGameScheduled(AUTO_RESET_DELAY)
        }
    }

    fun endGame() {
        gameEnded = true

        val fragment = findChildFragment(R.id.fragment_agent_board)
        if (fragment is AgentBoardCellsFragment) {
            fragment.clickEnabled(false)
        }
    }

    fun resetGame() {
        Logger.debug("RESET GAME")
        resetGameScheduled()
    }

    private fun resetGameScheduled(delay: Long = 0L) {
        resetJob?.cancel()
        resetJob = lifecycleScope.launch {
            delay(delay)
            initGame()
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