package org.tensorflow.lite.examples.reinforcementlearning

import android.content.Context
import org.tensorflow.lite.support.model.Model
import org.tensorflow.litex.AssetFileLiteModel
import org.tensorflow.litex.TFLiteModel

abstract class PlaneStrikeAgent(context: Context,
                                model: String,
                                device: Model.Device,
                                numOfThreads: Int = 4,
                                useXNNPack: Boolean,
): AssetFileLiteModel(
    context,
    model,
    device,
    numOfThreads,
    useXNNPack
) {

    /** Predict the next move based on current board state.  */
    abstract fun predictNextMove(board: Array<Array<BoardCellStatus>>): Int

    /** Run model inference on current board state.  */
    protected abstract fun runInference()

    protected abstract fun prepareModelInput(board: Array<Array<BoardCellStatus?>?>?)

}