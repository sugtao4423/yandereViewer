package sugtao4423.yandereviewer;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class PostGridView extends RecyclerView{

	public PostGridView(Context context){
		super(context);
		init(context);
	}

	public PostGridView(Context context, AttributeSet attrs){
		super(context, attrs);
		init(context);
	}

	public void init(Context context){
		setHasFixedSize(true);
		setLayoutManager(new GridLayoutManager(context, 2));
		addItemDecoration(new GridSpacingItemDecoration(2, 30, true));
	}

}

/*
 * Thanks!!
 * https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing/
 */
class GridSpacingItemDecoration extends RecyclerView.ItemDecoration{

	private int spanCount;
	private int spacing;
	private boolean includeEdge;

	public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge){
		this.spanCount = spanCount;
		this.spacing = spacing;
		this.includeEdge = includeEdge;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
		int position = parent.getChildAdapterPosition(view);
		int column = position % spanCount;

		if(includeEdge){
			outRect.left = spacing - column * spacing / spanCount;
			outRect.right = (column + 1) * spacing / spanCount;

			if(position < spanCount)
				outRect.top = spacing;
			outRect.bottom = spacing;
		}else{
			outRect.left = column * spacing / spanCount;
			outRect.right = spacing - (column + 1) * spacing / spanCount;
			if(position >= spanCount)
				outRect.top = spacing;
		}
	}
}