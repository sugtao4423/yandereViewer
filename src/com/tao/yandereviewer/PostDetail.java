package com.tao.yandereviewer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class PostDetail extends Activity{

	private Post post;

	private TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_detail);
		text = (TextView)findViewById(R.id.postDetail_text);
		post = (Post)getIntent().getSerializableExtra("postdata");

		String tags = "";
		for(String s : post.getTags())
			tags += s + " ";
		tags = tags.substring(0, tags.length() - 1);

		getActionBar().setTitle(tags);
		setActionbarIcon();

		String str = "<p><strong>Statistics</strong><br />" +
				"Id: " + post.getId() + "<br />" +
				"Posted: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(post.getCreatedAt()) +
							" by " + post.getAuthor() + "<br />" +
				"Tags: " + tags + "<br />" +
				"Size: " + post.getFile().getWidth() + "x" + post.getFile().getHeight() + "<br />" +
				"Source: <a href=\"" + post.getSource() + "\">" + post.getSource() + "</a><br />" +
				"Rating: " + post.getRating() + "<br />" +
				"Score: " + post.getScore() + "<br /><br />" +
				"<strong>Preview</strong><br />" +
				"URL: <a href=\"" + post.getPreview().getUrl() + "\">" + post.getPreview().getUrl() + "</a><br />" +
				"Size: " + post.getPreview().getWidth() + "x" + post.getPreview().getHeight() + "<br /><br />" +
				"<strong>Sample</strong><br />" +
				"URL: <a href=\"" + post.getSample().getUrl() + "\">" + post.getSample().getUrl() + "</a><br />" +
				"Size: " + post.getSample().getWidth() + "x" + post.getSample().getHeight() + "<br />" +
				"File Size: " + getMB(post.getSample().getSize()) + "<br /><br />" +
				"<strong>File</strong><br />" +
				"URL: <a href=\"" + post.getFile().getUrl() + "\">" + post.getFile().getUrl() + "</a><br />" +
				"Size: " + post.getFile().getWidth() + "x" + post.getFile().getHeight() + "<br />" +
				"File Size: " + getMB(post.getFile().getSize()) + "</p>";
		text.setText(Html.fromHtml(str));
		text.setMovementMethod(LinkMovementMethod.getInstance());
	}

	public String getMB(int bytesize){
		DecimalFormat df = new DecimalFormat("#.#");
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		return df.format((float)bytesize / 1024 / 1024) + "MB";
	}

	public void setActionbarIcon(){
		final ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.drawable.ic_action_refresh);
		String path = getApplicationContext().getCacheDir().getAbsolutePath() + "/web_image_cache/" +
				post.getPreview().getUrl().replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
		File img = new File(path);
		if(img.exists()){
			Drawable d = Drawable.createFromPath(path);
			actionBar.setIcon(d);
		}else{
			new AsyncTask<Void, Void, Drawable>(){

				@Override
				protected Drawable doInBackground(Void... params){
					try{
						HttpURLConnection connection = (HttpURLConnection)new URL(post.getPreview().getUrl()).openConnection();
						connection.connect();
						InputStream is = connection.getInputStream();
						Bitmap bmp = BitmapFactory.decodeStream(is);
						is.close();
						connection.disconnect();
						return new BitmapDrawable(getResources(), bmp);
					}catch(IOException e){
						return null;
					}
				}

				@Override
				protected void onPostExecute(Drawable d){
					if(d != null)
						actionBar.setIcon(d);
				}
			}.execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "共有").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == Menu.FIRST){
			Intent i = new Intent();
			i.setAction(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_TEXT, new Yandere4j().getShareText(post, false));
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}
}
