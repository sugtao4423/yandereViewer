package com.tao.yandereviewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.tenthbit.view.ZoomImageView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

public class ShowImage extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		final ZoomImageView image = new ZoomImageView(this);
		setContentView(image);
		Intent i = getIntent();
		final String url = i.getStringExtra("url");
		final int size = i.getIntExtra("filesize", -1);

		new AsyncTask<Void, Integer, Bitmap>(){
			private ProgressDialog progDailog;

			@Override
			protected void onPreExecute(){
				progDailog = new ProgressDialog(ShowImage.this);
				progDailog.setMessage("Loading...");
				progDailog.setIndeterminate(false);
				progDailog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progDailog.setCancelable(true);
				progDailog.setMax(size);
				progDailog.setProgress(0);
				progDailog.show();
			}

			@Override
			protected Bitmap doInBackground(Void... params){
				try{
					HttpGet httpGet = new HttpGet(url);
					DefaultHttpClient httpClient = new DefaultHttpClient();

					InputStream input = httpClient.execute(httpGet).getEntity().getContent();
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					int len;
					for(int i = 0; (len = input.read(buffer)) > 0; ++i){
						bout.write(buffer, 0, len);
						publishProgress(i * 1024);
					}
					byte[] tmp = bout.toByteArray();
					Bitmap myBitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
					input.close();
					httpClient.getConnectionManager().shutdown();
					return myBitmap;
				}catch(IOException e){
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... val){
				progDailog.setProgress(val[0]);
			}

			@Override
			protected void onPostExecute(Bitmap bmp){
				progDailog.dismiss();
				if(bmp == null){
					Toast.makeText(ShowImage.this, "開けませんでした...", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				image.setImageBitmap(bmp);
			}
		}.execute();
	}
}