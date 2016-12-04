package com.tao.yandereviewer;

import java.util.ArrayList;

import android.content.Context;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import yandere4j.data.Post;

public class PostAdapter extends CardGridArrayAdapter{

	private Context context;
	private OnCardClickListener listener;

	public PostAdapter(Context context, OnCardClickListener listener){
		super(context, new ArrayList<Card>());
		this.context = context;
		this.listener = listener;
	}

	public void add(Post post, long readedId){
		PostCard card = new PostCard(context, post, readedId);
		card.setOnClickListener(listener);
		add(card);
	}

	public void addAll(Post[] post, long readedId){
		for(Post p : post)
			add(p, readedId);
	}
}
