package sugtao4423.yandereviewer

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PostGridView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    constructor(context: Context) : this(context, null)

    var gridLayoutManager: GridLayoutManager

    init {
        setHasFixedSize(true)
        gridLayoutManager = GridLayoutManager(context, 2)
        layoutManager = gridLayoutManager
        addItemDecoration(GridSpacingItemDecoration(2, 30, true))
    }

}

/*
 * Thanks!!
 * https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing/
 */
class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }

}