package com.dailystudio.tflite.example.reinforcementlearning.fragment

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.reinforcementlearning.*
import org.tensorflow.lite.examples.reinforcementlearning.fragment.AgentBoardCellsListFragment
import org.tensorflow.lite.examples.reinforcementlearning.fragment.PlayerBoardCellsListFragment
import org.tensorflow.lite.examples.reinforcementlearning.model.AgentBoardCellViewModel

class AgentBoardCellsFragment: AgentBoardCellsListFragment() {

    override fun onItemClick(
        recyclerView: RecyclerView,
        itemView: View,
        position: Int,
        item: AgentBoardCell,
        id: Long
    ) {
        val agentBoardViewModel = ViewModelProvider(this).get(
            AgentBoardCellViewModel::class.java)

        val playerActionX = position / Constants.BOARD_SIZE
        val playerActionY = position % Constants.BOARD_SIZE

        val agentCellId = BoardCell.getIdByPos(playerActionX, playerActionY)
        val agentCell = AgentBoardCellManager.get(agentCellId) ?: return

        if (item.status == BoardCellStatus.UNTRIED) {
            agentCell.status = if (item.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
                BoardCellStatus.HIT

//                playerHits++
//                playerHitsTextView?.setText("Agent board:\n$playerHits hits")
            } else {
                BoardCellStatus.MISS
            }

            agentBoardViewModel.updateAgentBoardCell(agentCell)
        }
    }
}