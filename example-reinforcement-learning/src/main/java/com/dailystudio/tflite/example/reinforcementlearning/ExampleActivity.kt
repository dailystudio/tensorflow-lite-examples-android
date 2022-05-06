package com.dailystudio.tflite.example.reinforcementlearning

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.reinforcementlearning.fragment.ReinforcementLearningFragment
import com.dailystudio.tflite.example.reinforcementlearning.viewmodel.AgentBoardViewModel
import com.dailystudio.tflite.example.reinforcementlearning.viewmodel.PlayerBoardViewModel

class ExampleActivity : AbsExampleActivity<InferenceInfo, Int>() {

    private var fabResetGame: View? = null

    private lateinit var agentBoardViewModel: AgentBoardViewModel
    private lateinit var playerBoardViewModel: PlayerBoardViewModel


    private var agentHitsPrompt: TextView? = null
    private var playerHitsPrompt: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        agentBoardViewModel = ViewModelProvider(this).get(AgentBoardViewModel::class.java)
        agentBoardViewModel.hits.value.observe(this) {
            playerHitsPrompt?.text = getString(R.string.player_hits_prompt, it)
        }
        playerBoardViewModel = ViewModelProvider(this).get(PlayerBoardViewModel::class.java)
        playerBoardViewModel.hits.value.observe(this) {
            agentHitsPrompt?.text = getString(R.string.agent_hits_prompt, it)
        }
    }

    override fun setupViews() {
        super.setupViews()

        playerHitsPrompt = findViewById(R.id.player_hits)
        agentHitsPrompt = findViewById(R.id.agent_hits)

        fabResetGame = findViewById(R.id.fab_reset)
        fabResetGame?.setOnClickListener {
            val fragment = exampleFragment
            if (fragment is ReinforcementLearningFragment) {
                fragment.resetGame()
            }
        }

    }
    override fun createBaseFragment(): Fragment {
        return ReinforcementLearningFragment()
    }

    override fun createResultsView(): View? {
        return null
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_example_reinforcement_learning
    }

    override fun onResultsUpdated(results: Int) {
    }

    override fun onInferenceInfoUpdated(info: InferenceInfo) {
        super.onInferenceInfoUpdated(info)
    }

    override fun getExampleName(): CharSequence? {
        return getString(R.string.app_name)
    }

    override fun getExampleIconResource(): Int {
        return R.drawable.about_icon
    }

    override fun getExampleDesc(): CharSequence? {
        return getString(R.string.app_desc)
    }

}
