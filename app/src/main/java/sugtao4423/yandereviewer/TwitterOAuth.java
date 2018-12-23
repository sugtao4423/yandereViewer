package sugtao4423.yandereviewer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterOAuth extends AppCompatActivity{

    public static final String CALLBACK_URL = "https://localhost/sugtao4423.yandereviewer/oauth";

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
                    rt = twitter.getOAuthRequestToken(CALLBACK_URL);
                    return true;
                }catch(TwitterException e){
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result){
                progDialog.dismiss();
                if(result){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rt.getAuthenticationURL()));
                    startActivity(intent);
                }else{
                    Toast.makeText(TwitterOAuth.this, getString(R.string.acquisition_of_request_token_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(intent == null || intent.getData() == null || !intent.getData().toString().startsWith(CALLBACK_URL))
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
                            .putString(Keys.TWITTER_AT, accessToken.getToken())
                            .putString(Keys.TWITTER_ATS, accessToken.getTokenSecret())
                            .putString(Keys.TWITTER_USERNAME, "@" + accessToken.getScreenName())
                            .commit();
                    Toast.makeText(TwitterOAuth.this, getString(R.string.cooperated), Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(TwitterOAuth.this, getString(R.string.acquisition_of_access_token_failed), Toast.LENGTH_LONG).show();
                }
                finish();
            }
        }.execute();
    }
}
