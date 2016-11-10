package com.tao.yandereviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterOAuth extends Activity{

	private Twitter twitter;
	private RequestToken rt;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		new AsyncTask<Void, Void, Boolean>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				progDialog = new ProgressDialog(TwitterOAuth.this);
				progDialog.setMessage("Loading...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDialog.setCancelable(true);
				progDialog.setCanceledOnTouchOutside(false);
				progDialog.show();
			}

			@Override
			protected Boolean doInBackground(Void... params){
				String ck = getString(R.string.twitter_ck);
				String cs = getString(R.string.twitter_cs);
				Configuration conf = new ConfigurationBuilder().setOAuthConsumerKey(ck).setOAuthConsumerSecret(cs).build();
				twitter = new TwitterFactory(conf).getInstance();
				try{
					rt = twitter.getOAuthRequestToken("yande.re-viewer://twitter");
					return true;
				}catch(TwitterException e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				progDialog.dismiss();
				if(result)
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rt.getAuthenticationURL())));
				else
					Toast.makeText(TwitterOAuth.this, "RequestTokenの取得に失敗しました", Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		if(intent == null || intent.getData() == null || !intent.getData().toString().startsWith("yande.re-viewer://twitter"))
			return;

		final String verifier = intent.getData().getQueryParameter("oauth_verifier");

		new AsyncTask<Void, Void, AccessToken>(){
			private ProgressDialog progDialog;

			@Override
			protected void onPreExecute(){
				progDialog = new ProgressDialog(TwitterOAuth.this);
				progDialog.setMessage("Loading...");
				progDialog.setIndeterminate(false);
				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDialog.setCancelable(true);
				progDialog.setCanceledOnTouchOutside(false);
				progDialog.show();
			}

			@Override
			protected AccessToken doInBackground(Void... params){
				try{
					return twitter.getOAuthAccessToken(rt, verifier);
				}catch(Exception e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(AccessToken accessToken){
				progDialog.dismiss();
				if(accessToken != null){
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
						.putString("twitter_at", accessToken.getToken())
						.putString("twitter_ats", accessToken.getTokenSecret())
						.putString("twitter_username", "@" + accessToken.getScreenName())
					.commit();
					Toast.makeText(TwitterOAuth.this, "連携しました", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(TwitterOAuth.this, "アクセストークンの取得に失敗しました", Toast.LENGTH_LONG).show();
				}
				finish();
			}
		}.execute();
	}
}
