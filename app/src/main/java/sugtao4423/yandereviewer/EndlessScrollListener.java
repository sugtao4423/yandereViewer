package sugtao4423.yandereviewer;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener{

    int visibleThreshold = 5;
    int visibleItemCount, totalItemCount;
    private int previousTotal = 0;
    private boolean loading = true;
    private boolean stopOnLoadMore = false;
    private int current_page = 0;

    private GridLayoutManager mGridLayoutManager;

    public EndlessScrollListener(GridLayoutManager gridLayoutManager){
        this.mGridLayoutManager = gridLayoutManager;
    }

    public void setVisibleThreshold(int visibleThreshold){
        this.visibleThreshold = visibleThreshold;
    }

    public void setStopOnLoadMore(boolean isStop){
        this.stopOnLoadMore = isStop;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy){
        super.onScrolled(recyclerView, dx, dy);

        if(stopOnLoadMore)
            return;

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mGridLayoutManager.getItemCount();
        int lastVisibleItem = mGridLayoutManager.findLastVisibleItemPosition();

        if(loading && totalItemCount > previousTotal){
            loading = false;
            previousTotal = totalItemCount;
        }

        if(!loading && (lastVisibleItem + visibleThreshold) > totalItemCount){
            current_page++;
            onLoadMore(current_page);
            loading = true;
        }
    }

    public void resetState(){
        this.current_page = 0;
        this.previousTotal = 0;
        this.loading = true;
        this.stopOnLoadMore = false;
    }

    public abstract void onLoadMore(int current_page);
}