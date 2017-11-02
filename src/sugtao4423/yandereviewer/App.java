package sugtao4423.yandereviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import sugtao4423.progressdialog.ProgressDialog;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class App extends Application{

	private boolean clearedHistory = false;

	public void setClearedHistory(boolean clearedHistory){
		this.clearedHistory = clearedHistory;
	}

	public boolean getClearedHistory(){
		return clearedHistory;
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

	public void saveImages(final Context context, final Post[] saveList){
		final NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
				Environment.DIRECTORY_DOWNLOADS + "/";
		final String saveDir = PreferenceManager.getDefaultSharedPreferences(context).getString(Keys.SAVEDIR, defaultPath);

		new AsyncTask<Void, Void, Void>(){

			@SuppressWarnings("deprecation")
			@Override
			protected Void doInBackground(Void... params){
				for(int i = 0; i < saveList.length; i++){
					Post current = saveList[i];
					Notification.Builder builder;
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
						builder = new Notification.Builder(context, "");
					else
						builder = new Notification.Builder(context);
					builder.setContentTitle("Saving... " + (i + 1) + "/" + saveList.length)
					.setContentText(new Yandere4j().getFileName(current))
					.setSmallIcon(android.R.drawable.stat_sys_download)
					.setProgress(100, 0, false)
					.setOngoing(true);
					nm.notify(i, builder.build());

					String path = saveDir + new Yandere4j().getFileName(current);

					try{
						HttpURLConnection conn = (HttpURLConnection)new URL(current.getFile().getUrl()).openConnection();
						conn.setRequestProperty("User-Agent", Yandere4j.USER_AGENT);
						conn.connect();
						InputStream is = conn.getInputStream();
						FileOutputStream fos = new FileOutputStream(path);
						byte[] buffer = new byte[1024];
						int len;
						int percentage = 0;
						for(int j = 0; (len = is.read(buffer)) > 0; ++j){
							fos.write(buffer, 0, len);
							int currentPer = Math.round((float)j * 1024 / current.getFile().getSize() * 100);
							if(percentage != currentPer){
								builder.setProgress(100, currentPer, false);
								nm.notify(i, builder.build());
								percentage = currentPer;
							}
						}
						fos.close();
						is.close();
						conn.disconnect();
					}catch(IOException e){
						File f = new File(path);
						if(f.exists())
							f.delete();
						String url = "https://yande.re/post/show/" + current.getId();
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, 0);
						builder.setProgress(0, 0, false)
						.setContentTitle(getString(R.string.save_failed) + " " + (i + 1) + "/" + saveList.length)
						.setSmallIcon(android.R.drawable.stat_sys_download_done)
						.setOngoing(false)
						.setContentIntent(pendingIntent);
						nm.notify(i, builder.build());
						continue;
					}
					Intent picIntent = new Intent(Intent.ACTION_VIEW);
					picIntent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
					PendingIntent contentIntent = PendingIntent.getActivity(context, 1, picIntent, 0);
					builder.setProgress(0, 0, false)
					.setContentTitle(getString(R.string.save_success) + " " + (i + 1) + "/" + saveList.length)
					.setSmallIcon(android.R.drawable.stat_sys_download_done)
					.setOngoing(false)
					.setContentIntent(contentIntent)
					.setAutoCancel(true);
					nm.notify(i, builder.build());
				}
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
}
