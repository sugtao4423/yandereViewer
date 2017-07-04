package sugtao4423.yandereviewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tenthbit.view.ZoomImageView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import yandere4j.Yandere4j;

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
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				progDialog = new ProgressDialog(ShowImage.this);
				progDialog.setMessage("Loading...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progDialog.setMax(size);
				progDialog.setProgress(0);
				progDialog.setCancelable(true);
				progDialog.setCanceledOnTouchOutside(false);
				progDialog.setOnCancelListener(new OnCancelListener(){
					@Override
					public void onCancel(DialogInterface dialog){
						cancel(true);
						finish();
					}
				});
				progDialog.show();
			}

			@Override
			protected Bitmap doInBackground(Void... params){
				try{
					HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
					conn.setRequestProperty("User-Agent", Yandere4j.USER_AGENT);
					conn.connect();
					InputStream is = conn.getInputStream();
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					int len;
					for(int i = 0; (len = is.read(buffer)) > 0; ++i){
						if(isCancelled()){
							conn.disconnect();
							is.close();
							bout.close();
							break;
						}
						bout.write(buffer, 0, len);
						publishProgress(i * 1024);
					}
					byte[] tmp = bout.toByteArray();
					Bitmap myBitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
					is.close();
					conn.disconnect();
					return myBitmap;
				}catch(IOException e){
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... val){
				progDialog.setProgress(val[0]);
			}

			@Override
			protected void onPostExecute(Bitmap bmp){
				progDialog.dismiss();
				if(bmp == null){
					Toast.makeText(ShowImage.this, getString(R.string.could_not_open), Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				image.setImageBitmap(bmp);
			}

			@Override
			protected void onCancelled(){
				Toast.makeText(ShowImage.this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}
}
