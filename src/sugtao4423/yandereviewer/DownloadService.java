package sugtao4423.yandereviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class DownloadService extends Service{

	public static final String INTENT_KEY_POSTS = "posts";

	private NotificationManager notificationManager;

	@TargetApi(26)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Post[] posts = (Post[])intent.getSerializableExtra(INTENT_KEY_POSTS);
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			int notifiId = 1;
			Intent appIntent = new Intent(this, MainActivity.class);
			appIntent.setAction(Intent.ACTION_MAIN);
			appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notifiId, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			String channelId = "default";
			String title = getString(R.string.app_name) + " download";
			NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
			Notification notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
					.setContentTitle(title)
					.setSmallIcon(R.drawable.ic_launcher)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent)
					.setWhen(System.currentTimeMillis())
					.build();
			startForeground(notifiId, notification);
		}
		saveImages(posts);
		return super.onStartCommand(intent, flags, startId);
	}

	private void saveImages(final Post[] saveList){
		new AsyncTask<Void, Void, Void>(){
			private String saveDir;

			@Override
			protected void onPreExecute(){
				String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
						Environment.DIRECTORY_DOWNLOADS + "/";
				saveDir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Keys.SAVEDIR, defaultPath);
			}

			@Override
			protected Void doInBackground(Void... params){
				for(int i = 0; i < saveList.length; i++){
					Post current = saveList[i];
					long time = System.currentTimeMillis();
					int notifiId = Integer.parseInt(String.valueOf(time).substring(String.valueOf(time).length() - 9, String.valueOf(time).length()));
					NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default")
					.setContentTitle("Saving... " + (i + 1) + "/" + saveList.length)
					.setContentText(new Yandere4j().getFileName(current))
					.setSmallIcon(android.R.drawable.stat_sys_download)
					.setProgress(100, 0, false)
					.setOngoing(true)
					.setWhen(time);
					notificationManager.notify(notifiId, builder.build());

					String path = saveDir + new Yandere4j().getFileName(current);
					try{
						doDownload(current, builder, notifiId, path);
					}catch(IOException e){
						onIOErrorNotification(current, builder, notifiId, path, i, saveList.length);
						continue;
					}
					onSuccessNotification(builder, notifiId, path, i, saveList.length);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result){
				stopSelf();
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void doDownload(Post post, NotificationCompat.Builder builder, int notifiId, String savePath) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)new URL(post.getFile().getUrl()).openConnection();
		conn.setRequestProperty("User-Agent", Yandere4j.USER_AGENT);
		conn.connect();
		InputStream is = conn.getInputStream();
		FileOutputStream fos = new FileOutputStream(savePath);
		byte[] buffer = new byte[1024];
		int len;
		int percentage = 0;
		for(int i = 0; (len = is.read(buffer)) > 0; ++i){
			fos.write(buffer, 0, len);
			int currentPer = Math.round((float)i * 1024 / post.getFile().getSize() * 100);
			if(percentage != currentPer){
				builder.setProgress(100, currentPer, false);
				notificationManager.notify(notifiId, builder.build());
				percentage = currentPer;
			}
		}
		fos.close();
		is.close();
		conn.disconnect();
	}

	private void onIOErrorNotification(Post post, NotificationCompat.Builder builder, int notifiId, String savePath, int currentPos, int allPostSize){
		try{
			Thread.sleep(200);
		}catch(InterruptedException e2){
		}
		File f = new File(savePath);
		if(f.exists())
			f.delete();
		String url = "https://yande.re/post/show/" + post.getId();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notifiId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setProgress(0, 0, false)
		.setContentTitle(getString(R.string.save_failed) + " " + (currentPos + 1) + "/" + allPostSize)
		.setSmallIcon(android.R.drawable.stat_sys_download_done)
		.setOngoing(false)
		.setContentIntent(pendingIntent)
		.setAutoCancel(false);
		notificationManager.notify(notifiId, builder.build());
	}

	private void onSuccessNotification(NotificationCompat.Builder builder, int notifiId, String savePath, int currentPos, int allPostSize){
		try{
			Thread.sleep(200);
		}catch(InterruptedException e){
		}
		Intent picIntent = new Intent(Intent.ACTION_VIEW);
		picIntent.setDataAndType(Uri.fromFile(new File(savePath)), "image/*");
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), notifiId, picIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setProgress(0, 0, false)
		.setContentTitle(getString(R.string.save_success) + " " + (currentPos + 1) + "/" + allPostSize)
		.setSmallIcon(android.R.drawable.stat_sys_download_done)
		.setOngoing(false)
		.setContentIntent(contentIntent)
		.setAutoCancel(true);
		notificationManager.notify(notifiId, builder.build());
	}

	@Override
	public IBinder onBind(Intent intent){
		return null;
	}

}
