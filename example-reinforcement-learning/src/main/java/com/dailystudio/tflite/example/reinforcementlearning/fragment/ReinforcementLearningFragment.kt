package com.dailystudio.tflite.example.reinforcementlearning.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.DevBricksFragment
import com.dailystudio.tflite.example.reinforcementlearning.R
import com.dailystudio.tflite.example.reinforcementlearning.viewmodel.BoardViewModel
import com.dailystudio.tflite.example.reinforcementlearning.viewmodel.GameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReinforcementLearningFragment: DevBricksFragment() {

    companion object {
        const val AUTO_RESET_DELAY = 2000L
    }

    private lateinit var boardViewModel: BoardViewModel

    private var resetJob: Job? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        boardViewModel = ViewModelProvider(requireActivity())
            .get(BoardViewModel::class.java)
        boardViewModel.gameState.observe(this) {
            val gameEnded = boardViewModel.isGameEnded()
            if (gameEnded) {
                resetGameScheduled(AUTO_RESET_DELAY)
            }
            enablePlayerClicks(!gameEnded)
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
        boardViewModel.resetGame()
    }

    fun endGame() {
        enablePlayerClicks(false)
    }

    private fun enablePlayerClicks(enabled: Boolean) {
        val fragment = findChildFragment(R.id.fragment_agent_board)
        if (fragment is AgentBoardCellsFragment) {
            fragment.clickEnabled(enabled)
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

}