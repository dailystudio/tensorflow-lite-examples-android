package com.dailystudio.tflite.example.reinforcementlearning.fragment

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dailystudio.tflite.example.reinforcementlearning.viewmodel.BoardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.reinforcementlearning.*
import org.tensorflow.lite.examples.reinforcementlearning.fragment.AgentBoardCellsListFragment

class AgentBoardCellsFragment: AgentBoardCellsListFragment() {

    private var blockClicks: Boolean = false

    fun clickEnabled(enabled: Boolean) {
        blockClicks = !enabled
    }

    override fun onItemClick(
        recyclerView: RecyclerView,
        itemView: View,
        position: Int,
        item: AgentBoardCell,
        id: Long
    ) {

        if (blockClicks) {
            return
        }

        val bardViewModel = ViewModelProvider(requireActivity()).get(
            BoardViewModel::class.java)

        val playerActionX = position / Constants.BOARD_SIZE
        val playerActionY = position % Constants.BOARD_SIZE

        lifecycleScope.launch(Dispatchers.IO) {
            val agentStrikePos =
                bardViewModel.playerActionOn(playerActionX, playerActionY)

            val agentActionX = agentStrikePos / Constants.BOARD_SIZE
            val agentActionY = agentStrikePos % Constants.BOARD_SIZE

            bardViewModel.agentActionOn(agentActionX, agentActionY)
        }
    }
}