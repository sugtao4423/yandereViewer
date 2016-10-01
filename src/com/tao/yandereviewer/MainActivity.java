package com.tao.yandereviewer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;
import yandere4j.Yandere;
import yandere4j.data.Post;

public class MainActivity extends Activity implements OnRefreshListener{

	private GridView grid;
	private SwipeRefreshLayout swipeRefresh;
	private PostAdapter adapter;
	private Yandere yandere;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		grid = (GridView)findViewById(R.id.grid);
		grid.setNumColumns(GridView.AUTO_FIT);
		grid.setVerticalSpacing(15);
		adapter = new PostAdapter(this);
		grid.setAdapter(adapter);

		swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
		swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"));
		swipeRefresh.setOnRefreshListener(this);

		yandere = new Yandere();
		loadPosts(false);
	}

	public void loadPosts(final boolean isRefresh){
		new AsyncTask<Void, Void, Post[]>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				if(!isRefresh){
					progDailog = new ProgressDialog(MainActivity.this);
					progDailog.setMessage("Loading...");
					progDailog.setIndeterminate(false);
					progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progDailog.setCancelable(true);
					progDailog.show();
				}
			}

			@Override
			protected Post[] doInBackground(Void... params){
				try{
					return yandere.getPosts();
				}catch(KeyManagementException | NoSuchAlgorithmException | JSONException | IOException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Post[] result){
				if(!isRefresh)
					progDailog.dismiss();
				else
					swipeRefresh.setRefreshing(false);
				if(result == null){
					Toast.makeText(MainActivity.this, "取得エラー", Toast.LENGTH_LONG).show();
					return;
				}
				adapter.clear();
				adapter.addAll(result);
			}
		}.execute();
	}

	@Override
	public void onRefresh(){
		loadPosts(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		return super.onOptionsItemSelected(item);
	}
}
