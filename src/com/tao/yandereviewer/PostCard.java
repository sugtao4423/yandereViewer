package com.tao.yandereviewer;

import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import it.gmariotti.cardslib.library.internal.Card;
import yandere4j.data.Post;

public class PostCard extends Card{

	protected SmartImageView image;
	protected TextView imageSize;
	private Post post;

	public PostCard(Context context, Post post){
		super(context, R.layout.grid_item_layout);
		this.post = post;
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view){
		image = (SmartImageView)parent.findViewById(R.id.postImage);
		imageSize = (TextView)parent.findViewById(R.id.imageSize);

		if(post.getMD5().equals("LOADMORE")){
			image.setImageResource(R.drawable.plus);
			imageSize.setText("Load More");
			return;
		}
		image.setImageUrl(post.getPreview().getUrl(), null, R.drawable.ic_action_refresh);
		imageSize.setText(post.getFile().getWidth() + "x" + post.getFile().getHeight());
	}

	public Post getPost(){
		return post;
	}
}
