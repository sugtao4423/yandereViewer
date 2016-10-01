package com.tao.yandereviewer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;
import yandere4j.Yandere;
import yandere4j.data.Post;

public class MainActivity extends Activity{

	private GridView grid;
	private PostAdapter adapter;
	private Yandere yandere;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		grid = new GridView(this);
		grid.setNumColumns(GridView.AUTO_FIT);
		grid.setVerticalSpacing(15);
		setContentView(grid);
		adapter = new PostAdapter(this);
		grid.setAdapter(adapter);
		yandere = new Yandere();
		loadPosts();
	}

	public void loadPosts(){
		new AsyncTask<Void, Void, Post[]>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				progDailog = new ProgressDialog(MainActivity.this);
				progDailog.setMessage("Loading...");
				progDailog.setIndeterminate(false);
				progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDailog.setCancelable(true);
				progDailog.show();
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
				progDailog.dismiss();
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
	public boolean onCreateOptionsMenu(Menu menu){
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		return super.onOptionsItemSelected(item);
	}
}
