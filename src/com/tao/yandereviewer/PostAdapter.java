package com.tao.yandereviewer;

import com.loopj.android.image.SmartImageView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import yandere4j.data.Post;

public class PostAdapter extends ArrayAdapter<Post>{

	private LayoutInflater mInflater;

	public PostAdapter(Context context){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}
	
	class ViewHolder{
		SmartImageView image;
		TextView imageSize;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, final ViewGroup parent){
		final ViewHolder holder;
		final Post post = getItem(position);

		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_item_layout, null);
			SmartImageView image = (SmartImageView)convertView.findViewById(R.id.postImage);
			TextView imageSize = (TextView)convertView.findViewById(R.id.imageSize);

			holder = new ViewHolder();
			holder.image = image;
			holder.imageSize = imageSize;

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		if(post.getMD5().equals("LOADMORE")){
			holder.image.setImageResource(R.drawable.plus);
			holder.imageSize.setText("Load More");
			return convertView;
		}
		holder.image.setImageUrl(post.getPreview().getUrl());
		holder.imageSize.setText(post.getFile().getWidth() + "x" + post.getFile().getHeight());
		return convertView;
	}
}
