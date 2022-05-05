package org.tensorflow.lite.examples.reinforcementlearning

import android.graphics.Color
import android.view.View
import com.dailystudio.devbricksx.annotations.*
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsViewHolder
import com.dailystudio.tflite.example.reinforcementlearning.R

open class BoardCell(val x: Int,
                     val y: Int,
): InMemoryObject<String> {

    var status: BoardCellStatus = BoardCellStatus.UNTRIED
    var hiddenStatus: HiddenBoardCellStatus = HiddenBoardCellStatus.UNOCCUPIED

    companion object {

        fun getIdByPos(x: Int, y: Int) = buildString {
            append(x)
            append('_')
            append(y)
        }

    }

    override fun getKey() = getIdByPos(x, y)

    override fun toString(): String {
        return buildString {
            append("${this@BoardCell.javaClass.simpleName}: ")
            append("[$x, $y], ")
            append("status: $status ")
            append("hiddenStatus: $hiddenStatus ")
        }
    }
}

@ViewModel
@ListFragment (
    gridLayout = true,
    columns = Constants.BOARD_SIZE,
    layoutByName = "fragment_board"
)
@Adapter(
    viewType = ViewType.Customized,
    viewHolder = AgentBoardCellViewHolder::class,
    layoutByName = "layout_cell"
)
@DiffUtil
@InMemoryManager(key = String::class)
@InMemoryRepository(key = String::class)
class AgentBoardCell(x: Int,
                     y: Int,
): BoardCell(x, y)

@ViewModel
@ListFragment (
    gridLayout = true,
    columns = Constants.BOARD_SIZE,
    layoutByName = "fragment_board"

)
@Adapter(
    viewType = ViewType.Customized,
    viewHolder = BoardCellViewHolder::class,
    layoutByName = "layout_cell"
)
@DiffUtil
@InMemoryManager(key = String::class)
@InMemoryRepository(key = String::class)
class PlayerBoardCell(x: Int,
                      y: Int,
): BoardCell(x, y)

open class BoardCellViewHolder(view: View): AbsViewHolder<BoardCell>(view) {

    override fun bind(item: BoardCell) {
        val cellView: View? = itemView.findViewById(R.id.cell)
        val color = colorByStatus(item)

        Logger.debug("[COLOR: ${color}] item: $item")
        cellView?.setBackgroundColor(colorByStatus(item))
    }

    protected open fun colorByStatus(item: BoardCell): Int {
        return when (item.status) {
            BoardCellStatus.UNTRIED -> {
                if (item.hiddenStatus == HiddenBoardCellStatus.OCCUPIED_BY_PLANE) {
                    Color.BLUE
                } else {
                    Color.WHITE
                }
            }
            BoardCellStatus.HIT -> Color.RED
            else -> Color.YELLOW
        }
    }

}

class AgentBoardCellViewHolder(view: View): BoardCellViewHolder(view) {

    override fun colorByStatus(item: BoardCell): Int {
        return when(item.status) {
            BoardCellStatus.UNTRIED -> Color.WHITE
            else -> super.colorByStatus(item)
        }
    }

}
