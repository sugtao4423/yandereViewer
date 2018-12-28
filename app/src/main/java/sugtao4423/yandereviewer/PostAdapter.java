package sugtao4423.yandereviewer;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;

import java.util.ArrayList;
import java.util.HashMap;

import yandere4j.data.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private MainActivity mainActivity;
    private long readedId = -1L;
    private ArrayList<Post> data;
    private LayoutInflater inflater;
    private HashMap<Post, Integer> multiSelectedItems;
    private int cardViewBGColor;

    public PostAdapter(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        data = new ArrayList<Post>();
        inflater = (LayoutInflater)mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        readedId = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext()).getLong(Keys.READEDID, -1L);
        multiSelectedItems = new HashMap<Post, Integer>();
        cardViewBGColor = new CardView(mainActivity).getCardBackgroundColor().getDefaultColor();
    }

    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position){
        return new ViewHolder(inflater.inflate(R.layout.grid_item_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){
        if(!(data != null && data.size() > position && data.get(position) != null))
            return;
        final Post item = data.get(position);

        holder.itemView.setOnClickListener(mainActivity.getOnCardClickListener(item));
        holder.itemView.setOnLongClickListener(mainActivity.getOnCardLongClickListener());

        if(item.getMD5().equals("LOADMORE")){
            holder.image.setImageResource(R.drawable.plus);
            holder.imageSize.setText("Load More");
            holder.imageSize.setBackgroundColor(Color.parseColor("#ffffff"));
            return;
        }
        holder.image.setImageUrl(item.getPreview().getUrl(), null, R.drawable.ic_action_refresh);
        holder.imageSize.setText(item.getFile().getWidth() + "x" + item.getFile().getHeight());
        if(readedId < item.getId())
            holder.imageSize.setBackgroundColor(Color.parseColor("#e1bee7"));
        else
            holder.imageSize.setBackgroundColor(Color.parseColor("#ffffff"));
        if(multiSelectedItems.keySet().contains(item))
            holder.itemView.setBackgroundColor(Color.parseColor("#B3E5FC"));
        else
            holder.itemView.setBackgroundColor(cardViewBGColor);
    }

    @Override
    public int getItemCount(){
        if(data != null)
            return data.size();
        else
            return 0;
    }

    public void clear(){
        int size = data.size();
        data.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void add(Post post){
        data.add(post);
        notifyItemInserted(data.size() - 1);
    }

    public void addAll(Post[] posts){
        int pos = data.size();
        for(Post p : posts)
            data.add(p);
        notifyItemRangeInserted(pos, posts.length);
    }

    public void remove(Post post){
        int pos = data.indexOf(post);
        data.remove(post);
        notifyItemRemoved(pos);
    }

    public boolean isPostSelected(Post post){
        return multiSelectedItems.keySet().contains(post);
    }

    public void setPostSelected(Post post, boolean isSelect){
        int pos = data.indexOf(post);
        if(isSelect)
            multiSelectedItems.put(post, pos);
        else
            multiSelectedItems.remove(post);
        notifyItemChanged(pos);
    }

    public Post[] getSelectedPosts(){
        return multiSelectedItems.keySet().toArray(new Post[multiSelectedItems.size()]);
    }

    public void clearSelectedPosts(){
        Integer[] destroySelectedPostPos = multiSelectedItems.values().toArray(new Integer[multiSelectedItems.size()]);
        multiSelectedItems.clear();
        for(int i : destroySelectedPostPos)
            notifyItemChanged(i);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        SmartImageView image;
        TextView imageSize;

        public ViewHolder(View v){
            super(v);
            image = (SmartImageView)v.findViewById(R.id.postImage);
            imageSize = (TextView)v.findViewById(R.id.imageSize);
        }
    }

}
