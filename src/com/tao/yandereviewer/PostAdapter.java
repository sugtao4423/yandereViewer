package com.tao.yandereviewer;

import java.util.ArrayList;

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayMultiChoiceAdapter;
import it.gmariotti.cardslib.library.view.base.CardViewWrapper;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.Card.OnLongCardClickListener;
import yandere4j.data.Post;

public class PostAdapter extends CardGridArrayMultiChoiceAdapter{

	private Activity activity;
	private OnCardClickListener listener;

	public PostAdapter(Activity activity, OnCardClickListener listener){
		super(activity, new ArrayList<Card>());
		this.activity = activity;
		this.listener = listener;
	}

	public void add(Post post, long readedId){
		PostCard card = new PostCard(activity, post, readedId);
		card.setOnClickListener(listener);
		card.setOnLongClickListener(new OnLongCardClickListener(){

			@Override
			public boolean onLongClick(Card card, View view){
				return startActionMode(activity);
			}
		});
		add(card);
	}

	public void addAll(Post[] post, long readedId){
		for(Post p : post)
			add(p, readedId);
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode arg0, int arg1, long arg2, boolean arg3, CardViewWrapper arg4, Card arg5){
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu){
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu){
		super.onCreateActionMode(mode, menu);
		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Save All");
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item){
		if(item.getItemId() == Menu.FIRST){
			ArrayList<Post> posts = new ArrayList<Post>();
			for(Card c : getSelectedCards())
				posts.add(((PostCard)c).getPost());
			((App)activity.getApplicationContext()).saveImages(activity, posts);
			mode.finish();
			return true;
		}
		return false;
	}
}
