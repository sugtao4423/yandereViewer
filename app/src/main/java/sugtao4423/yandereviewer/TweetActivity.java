package sugtao4423.yandereviewer;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class TweetActivity extends AppCompatActivity{

	public static final String INTENT_EXTRA_POST = "post";

	private Post post;
	private Twitter twitter;

	private Yandere4j yandere;

	private EditText editText;
	private ImageButton tweetBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet_activity);
		post = (Post)getIntent().getSerializableExtra(INTENT_EXTRA_POST);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Configuration conf = new ConfigurationBuilder()
				.setOAuthConsumerKey(getString(R.string.twitter_ck))
				.setOAuthConsumerSecret(getString(R.string.twitter_cs))
		.build();
		AccessToken at = new AccessToken(pref.getString(Keys.TWITTER_AT, null), pref.getString(Keys.TWITTER_ATS, null));
		twitter = new TwitterFactory(conf).getInstance(at);
		yandere = new Yandere4j();

		tweetBtn = (ImageButton)findViewById(R.id.tweetButton);
		editText = (EditText)findViewById(R.id.tweetText);
		addTextWatcher((TextView)findViewById(R.id.moji140));
		((TextView)findViewById(R.id.tweetAccount)).setText(pref.getString(Keys.TWITTER_USERNAME, null));

		editText.setText(yandere.getShareText(post));
	}

	public void tweet(View v){
		tweetBtn.setEnabled(false);
		new AsyncTask<Void, Void, Boolean>(){

			@Override
			protected Boolean doInBackground(Void... params){
				try{
					twitter.updateStatus(editText.getText().toString());
					return true;
				}catch(TwitterException e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(!result)
					Toast.makeText(TweetActivity.this, getString(R.string.tweet_failed), Toast.LENGTH_LONG).show();
				else
					Toast.makeText(TweetActivity.this, getString(R.string.tweet_success), Toast.LENGTH_SHORT).show();
			}
		}.execute();
		finish();
	}

	public void addTextWatcher(final TextView textView){
		editText.addTextChangedListener(new TextWatcher(){
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){
				textView.setText(String.valueOf(140 - s.length()));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){
			}

			@Override
			public void afterTextChanged(Editable s){
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(editText.getText().toString().length() > 140){
			menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, getString(R.string.undo)).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, getString(R.string.shorten)).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == Menu.FIRST){
			editText.setText(yandere.getShareText(post));
		}else if(item.getItemId() == Menu.FIRST + 1){
			String text = editText.getText().toString();
			String shortenTitle = yandere.getShareTitle(post);
			int otherLetterLength = text.length() - shortenTitle.length() - 1;
			shortenTitle = shortenTitle.substring(0, 140 - otherLetterLength - 4) + "...";
			editText.setText(text.replace(yandere.getShareTitle(post), shortenTitle));
		}
		return super.onOptionsItemSelected(item);
	}
}
