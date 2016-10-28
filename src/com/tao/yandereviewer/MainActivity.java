package com.tao.yandereviewer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class MainActivity extends Activity implements OnRefreshListener, OnItemClickListener{

	private GridView grid;
	private SwipeRefreshLayout swipeRefresh;
	private PostAdapter adapter;
	private Yandere4j yandere;
	private int yanderePage;

	private SharedPreferences pref;
	private boolean isShowFullSize;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		grid = (GridView)findViewById(R.id.grid);
		grid.setNumColumns(GridView.AUTO_FIT);
		grid.setVerticalSpacing(15);
		adapter = new PostAdapter(this);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(this);

		swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
		swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"));
		swipeRefresh.setOnRefreshListener(this);

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		loadSettings();

		yandere = new Yandere4j();
		yanderePage = 1;
		loadPosts(false);
	}

	public void loadPosts(final boolean isRefresh){
		if(isRefresh){
			adapter.clear();
			yanderePage = 1;
		}
		new AsyncTask<Void, Void, Post[]>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				if(!isRefresh){
					progDialog = new ProgressDialog(MainActivity.this);
					progDialog.setMessage("Loading...");
					progDialog.setIndeterminate(false);
					progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progDialog.setCancelable(true);
					progDialog.show();
				}
			}

			@Override
			protected Post[] doInBackground(Void... params){
				try{
					return yandere.getPosts(yanderePage);
				}catch(KeyManagementException | NoSuchAlgorithmException | JSONException | IOException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(Post[] result){
				if(!isRefresh)
					progDialog.dismiss();
				else
					swipeRefresh.setRefreshing(false);
				if(result == null){
					Toast.makeText(MainActivity.this, "取得エラー", Toast.LENGTH_LONG).show();
					return;
				}
				yanderePage++;
				adapter.addAll(result);
				Post load = new Post(null, null, null, -1, -1, -1, null, null, null, null, null,
						"LOADMORE", null, null, null, null, false, false, false, false, false, false, -1, -1, -1);
				adapter.add(load);
			}
		}.execute();
	}

	@Override
	public void onRefresh(){
		loadPosts(true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		final Post post = (Post)parent.getItemAtPosition(position);
		if(post.getMD5().equals("LOADMORE")){
			adapter.remove(post);
			loadPosts(false);
			return;
		}
		String[] items = new String[]{(isShowFullSize ? "フルサイズ" : "サンプルサイズ") + "を表示", "フルサイズをブラウザで開く", "フルサイズを保存", "共有", "詳細"};
		new AlertDialog.Builder(this)
		.setItems(items, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which){
				if(which == 0){
					Intent i = new Intent(MainActivity.this, ShowImage.class);
					if(isShowFullSize){
						i.putExtra("url", post.getFile().getUrl());
						i.putExtra("filesize", post.getFile().getSize());
					}else{
						i.putExtra("url", post.getSample().getUrl());
						i.putExtra("filesize", post.getSample().getSize());
					}
					startActivity(i);
				}else if(which == 1){
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getFile().getUrl()));
					startActivity(i);
				}else if(which == 2){
					saveImage(post);
				}else if(which == 3){
					Intent i = new Intent();
					i.setAction(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_TEXT, yandere.getShareText(post, false));
					startActivity(i);
				}else if(which == 4){
					Intent i = new Intent(MainActivity.this, PostDetail.class);
					i.putExtra("postdata", post);
					startActivity(i);
				}
			}
		}).show();
	}

	public void loadSettings(){
		isShowFullSize = pref.getBoolean("isShowFullSize", true);
	}

	public void saveImage(final Post post){
		new AsyncTask<Void, Integer, Boolean>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				progDialog = new ProgressDialog(MainActivity.this);
				progDialog.setMessage("Loading...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progDialog.setCancelable(true);
				progDialog.setMax(post.getFile().getSize());
				progDialog.setProgress(0);
				progDialog.show();
			}

			@Override
			protected Boolean doInBackground(Void... params){
				try{
					String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
							Environment.DIRECTORY_DOWNLOADS + "/" + yandere.getFileName(post);

					HttpGet httpGet = new HttpGet(post.getFile().getUrl());
					DefaultHttpClient httpClient = new DefaultHttpClient();
	
					InputStream is = httpClient.execute(httpGet).getEntity().getContent();
					FileOutputStream fos = new FileOutputStream(path);
					byte[] buffer = new byte[1024];
					int len;
					for(int i = 0; (len = is.read(buffer)) > 0; ++i){
						fos.write(buffer, 0, len);
						publishProgress(i * 1024);
					}
					fos.close();
					is.close();
					return true;
				}catch(IOException e){
					return false;
				}
			}

			@Override
			protected void onProgressUpdate(Integer... val){
				progDialog.setProgress(val[0]);
			}

			@Override
			protected void onPostExecute(Boolean result){
				progDialog.dismiss();
				if(!result)
					Toast.makeText(MainActivity.this, "保存に失敗しました...", Toast.LENGTH_LONG).show();
				else
					Toast.makeText(MainActivity.this, "保存しました", Toast.LENGTH_LONG).show();
			}
		}.execute();
	}

	@Override
	public void onResume(){
		super.onResume();
		loadSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "設定").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == Menu.FIRST)
			startActivity(new Intent(this, Settings.class));
		return super.onOptionsItemSelected(item);
	}
}
