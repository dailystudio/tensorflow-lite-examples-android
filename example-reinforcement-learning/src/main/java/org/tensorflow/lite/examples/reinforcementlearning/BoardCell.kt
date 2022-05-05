package org.tensorflow.lite.examples.reinforcementlearning

import android.view.View
import com.dailystudio.devbricksx.annotations.Adapter
import com.dailystudio.devbricksx.annotations.DiffUtil
import com.dailystudio.devbricksx.annotations.InMemoryManager
import com.dailystudio.devbricksx.annotations.InMemoryRepository
import com.dailystudio.devbricksx.inmemory.InMemoryObject
import com.dailystudio.devbricksx.ui.AbsViewHolder

open class Cell(val x: Int,
                val y: Int)

@Adapter(viewHolder = BoardCellViewHolder::class)
@DiffUtil
@InMemoryManager(key = Int::class)
@InMemoryRepository(key = Int::class)
class BoardCell(private val id: Int,
                x: Int,
                y: Int,
                val status: BoardCellStatus
): Cell(x, y), InMemoryObject<Int> {
    override fun getKey() = id
}

class BoardCellViewHolder(view: View): AbsViewHolder<BoardCell>(view) {
    override fun bind(item: BoardCell) {
    }


}
@Adapter(viewHolder = HiddenBoardCellViewHolder::class)
@DiffUtil
@InMemoryManager(key = Int::class)
@InMemoryRepository(key = Int::class)
class HiddenBoardCell(private val id: Int,
                      x: Int,
                      y: Int,
                      val status: HiddenBoardCellStatus
): Cell(x, y), InMemoryObject<Int> {
    override fun getKey() = id
}

class HiddenBoardCellViewHolder(view: View): AbsViewHolder<HiddenBoardCell>(view) {
    override fun bind(item: HiddenBoardCell) {
    }

}