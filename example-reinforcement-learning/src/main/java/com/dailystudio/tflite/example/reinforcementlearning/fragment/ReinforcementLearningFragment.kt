package com.dailystudio.tflite.example.reinforcementlearning.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.fragment.DevBricksFragment
import com.dailystudio.tflite.example.reinforcementlearning.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.reinforcementlearning.*
import java.io.IOException
import java.util.*

class ReinforcementLearningFragment: DevBricksFragment() {

    private var agentHits = 0
    private var playerHits = 0
    private val playerBoard = Array(Constants.BOARD_SIZE) {
        arrayOfNulls<BoardCellStatus>(
            Constants.BOARD_SIZE
        )
    }
    private val playerHiddenBoard = Array(Constants.BOARD_SIZE) {
        arrayOfNulls<HiddenBoardCellStatus>(
            Constants.BOARD_SIZE
        )
    }
    private val agentBoard = Array(Constants.BOARD_SIZE) {
        arrayOfNulls<BoardCellStatus>(
            Constants.BOARD_SIZE
        )
    }
    private val agentHiddenBoard = Array(Constants.BOARD_SIZE) {
        arrayOfNulls<HiddenBoardCellStatus>(
            Constants.BOARD_SIZE
        )
    }

    private var agentBoardGridView: GridView? = null
    private var playerBoardGridView: GridView? = null
    private var agentHitsTextView: TextView? = null
    private var playerHitsTextView: TextView? = null
    private var resetButton: Button? = null

    private var agent: PlaneStrikeAgent? = null

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

    fun setupViews(fragmentView: View) {
        val context = requireContext()

        agentBoardGridView = fragmentView.findViewById<View>(R.id.agent_board_gridview) as GridView
        playerBoardGridView = fragmentView.findViewById<View>(R.id.player_board_gridview) as GridView
        agentHitsTextView = fragmentView.findViewById<View>(R.id.agent_hits_textview) as TextView
        playerHitsTextView = fragmentView.findViewById<View>(R.id.player_hits_textview) as TextView
        initGame()
        agent = try {
            if (Constants.USE_MODEL_FROM_TF) {
                RLAgent(context)
            } else {
                RLAgentFromTFAgents(context)
            }
        } catch (e: IOException) {
            Log.e(
                Constants.TAG,
                e.message!!
            )
            return
        }

        playerBoardGridView?.setAdapter(
            BoardCellAdapter(context, playerBoard, playerHiddenBoard, false)
        )
        agentBoardGridView?.setAdapter(BoardCellAdapter(context, agentBoard, agentHiddenBoard, true))
        agentBoardGridView?.setOnItemClickListener(
            AdapterView.OnItemClickListener { adapterView, view, position, l -> // Player action
                val playerActionX = position / Constants.BOARD_SIZE
                val playerActionY = position % Constants.BOARD_SIZE
                if (agentBoard[playerActionX][playerActionY] == BoardCellStatus.UNTRIED) {
                    if (agentHiddenBoard[playerActionX][playerActionY]
                        == HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                    ) {
                        agentBoard[playerActionX][playerActionY] = BoardCellStatus.HIT
                        playerHits++
                        playerHitsTextView?.setText("Agent board:\n$playerHits hits")
                    } else {
                        agentBoard[playerActionX][playerActionY] = BoardCellStatus.MISS
                    }
                }

                // Agent action
                val agentStrikePosition = agent?.predictNextMove(playerBoard) ?: -1
                if (agentStrikePosition == -1) {
                    Toast.makeText(
                        context,
                        "Something went wrong with the RL agent! Please restart the app.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@OnItemClickListener
                }
                val agentStrikePositionX = agentStrikePosition / Constants.BOARD_SIZE
                val agentStrikePositionY = agentStrikePosition % Constants.BOARD_SIZE
                if (playerHiddenBoard[agentStrikePositionX][agentStrikePositionY]
                    == HiddenBoardCellStatus.OCCUPIED_BY_PLANE
                ) {
                    // Hit
                    playerBoard[agentStrikePositionX][agentStrikePositionY] = BoardCellStatus.HIT
                    agentHits++
                    agentHitsTextView?.setText("Player board:\n$agentHits hits")
                } else {
                    // Miss
                    playerBoard[agentStrikePositionX][agentStrikePositionY] = BoardCellStatus.MISS
                }
                if (agentHits == Constants.PLANE_CELL_COUNT
                    || playerHits == Constants.PLANE_CELL_COUNT
                ) {
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
                    val resetGameTimer = Timer()
                    resetGameTimer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    initGame()
                                }
                            }
                        },
                        2000
                    )
                }
                agentBoardGridView?.invalidateViews()
                playerBoardGridView?.invalidateViews()
            })
        resetButton = fragmentView.findViewById<View>(R.id.reset_button) as Button
        resetButton?.setOnClickListener(
            View.OnClickListener { initGame() })
    }

    private fun initGame() {
        initBoard(playerBoard)
        placePlaneOnHiddenBoard(playerHiddenBoard)
        initBoard(agentBoard)
        placePlaneOnHiddenBoard(agentHiddenBoard)
        agentBoardGridView!!.invalidateViews()
        playerBoardGridView!!.invalidateViews()
        agentHits = 0
        playerHits = 0
        agentHitsTextView!!.text = "Player board:\n0 hits"
        playerHitsTextView!!.text = "Agent board:\n0 hits"
    }

    private fun initBoard(board: Array<Array<BoardCellStatus?>>) {
        for (i in 0 until Constants.BOARD_SIZE) {
            Arrays.fill(board[i], 0, Constants.BOARD_SIZE, BoardCellStatus.UNTRIED)
        }
    }

    private fun initHiddenBoard(board: Array<Array<HiddenBoardCellStatus?>>) {
        for (i in 0 until Constants.BOARD_SIZE) {
            Arrays.fill(board[i], 0, Constants.BOARD_SIZE, HiddenBoardCellStatus.UNOCCUPIED)
        }
    }

    private fun placePlaneOnHiddenBoard(hiddenBoard: Array<Array<HiddenBoardCellStatus?>>) {
        initHiddenBoard(hiddenBoard)

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