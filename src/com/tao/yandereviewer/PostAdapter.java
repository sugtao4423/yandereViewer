package com.tao.yandereviewer;

import java.util.ArrayList;

import android.content.Context;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import yandere4j.data.Post;

public class PostAdapter extends CardGridArrayAdapter{

	private Context context;

	public PostAdapter(Context context){
		super(context, new ArrayList<Card>());
		this.context = context;
	}

	public void add(Post post, OnCardClickListener listener){
		PostCard card = new PostCard(context, post);
		card.setOnClickListener(listener);
		add(card);
	}

	public void addAll(Post[] post, OnCardClickListener listener){
		for(Post p : post)
			add(p, listener);
	}
}
