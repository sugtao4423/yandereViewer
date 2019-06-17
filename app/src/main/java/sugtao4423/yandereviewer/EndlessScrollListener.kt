package sugtao4423.yandereviewer

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView

abstract class EndlessScrollListener(private val gridLayoutManager: GridLayoutManager) : RecyclerView.OnScrollListener() {

    var visibleThreshold = 5
    var visibleItemCount = -1
    var totalItemCount = -1
    private var previousTotal = 0
    private var loading = true
    var stopOnLoadMore = false
    private var currentPage = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (stopOnLoadMore) {
            return
        }

        visibleItemCount = recyclerView.childCount
        totalItemCount = gridLayoutManager.itemCount
        val lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition()

        if (loading && totalItemCount > previousTotal) {
            loading = false
            previousTotal = totalItemCount
        }

        if (!loading && (lastVisibleItem + visibleThreshold) > totalItemCount) {
            currentPage++
            onLoadMore(currentPage)
            loading = true
        }
    }

    fun resetState() {
        this.currentPage = 0
        this.previousTotal = 0
        this.loading = true
        this.stopOnLoadMore = false
    }

    abstract fun onLoadMore(currentPage: Int)

}