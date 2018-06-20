package sugtao4423.yandereviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import sugtao4423.progressdialog.ProgressDialog;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class App extends Application{

	private boolean clearedHistory = false;
	private boolean isRefreshTags = false;

	public void setClearedHistory(boolean clearedHistory){
		this.clearedHistory = clearedHistory;
	}

	public boolean getClearedHistory(){
		return clearedHistory;
	}

	public void setIsRefreshTags(boolean isRefreshTags){
		this.isRefreshTags = isRefreshTags;
	}

	public boolean getIsRefreshTags(){
		return isRefreshTags;
	}

	public void saveImage(final Context context, final Post post){
		new AsyncTask<Void, Integer, Boolean>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				progDialog = new ProgressDialog(context);
				progDialog.setMessage("Saving...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progDialog.setMax(post.getFile().getSize());
				progDialog.setProgress(0);
				progDialog.setCancelable(true);
				progDialog.setCanceledOnTouchOutside(false);
				progDialog.setOnCancelListener(new OnCancelListener(){
					@Override
					public void onCancel(DialogInterface dialog){
						cancel(true);
					}
				});
				progDialog.show();
			}

			@Override
			protected Boolean doInBackground(Void... params){
				try{
					String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
							Environment.DIRECTORY_DOWNLOADS + "/";
					String saveDir = PreferenceManager.getDefaultSharedPreferences(context).getString(Keys.SAVEDIR, defaultPath);
					String path = saveDir + new Yandere4j().getFileName(post);

					HttpURLConnection conn = (HttpURLConnection)new URL(post.getFile().getUrl()).openConnection();
					conn.setRequestProperty("User-Agent", Yandere4j.USER_AGENT);
					conn.connect();
					InputStream is = conn.getInputStream();
					FileOutputStream fos = new FileOutputStream(path);
					byte[] buffer = new byte[1024];
					int len;
					for(int i = 0; (len = is.read(buffer)) > 0; ++i){
						if(isCancelled()){
							conn.disconnect();
							is.close();
							fos.close();
							File file = new File(path);
							if(file.exists())
								file.delete();
							break;
						}
						fos.write(buffer, 0, len);
						publishProgress(i * 1024);
					}
					fos.close();
					is.close();
					conn.disconnect();
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
					Toast.makeText(context, getString(R.string.save_failed), Toast.LENGTH_LONG).show();
				else
					Toast.makeText(context, getString(R.string.save_success), Toast.LENGTH_LONG).show();
			}

			@Override
			protected void onCancelled(){
				Toast.makeText(context, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}

}
