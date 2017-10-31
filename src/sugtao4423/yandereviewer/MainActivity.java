package sugtao4423.yandereviewer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.view.CardGridView;
import sugtao4423.icondialog.IconDialog;
import sugtao4423.icondialog.IconItem;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class MainActivity extends Activity implements OnRefreshListener{

	public static final String INTENT_EXTRA_SEARCHQUERY = "searchQuery";

	private static final int SAMPLE = 0;
	private static final int FULL = 1;
	private static final int ASK = 2;

	private App app;

	private CardGridView grid;
	private SwipeRefreshLayout swipeRefresh;
	private PostAdapter adapter;
	private Yandere4j yandere;
	private int yanderePage;
	private long readedId;
	private String searchQuery;

	private SharedPreferences pref;
	private int howView;
	private String howViewStr;
	private SQLiteDatabase db;

	private Twitter twitter;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		grid = (CardGridView)findViewById(R.id.grid);
		grid.setNumColumns(GridView.AUTO_FIT);
		grid.setVerticalSpacing(15);
		adapter = new PostAdapter(this, getCardClickListener());
		grid.setAdapter(adapter);
		grid.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
		swipeRefresh.setColorSchemeColors(Color.parseColor("#2196F3"));
		swipeRefresh.setOnRefreshListener(this);

		app = (App)getApplicationContext();
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		yandere = new Yandere4j();

		loadSettings();
		yanderePage = 1;
		readedId = pref.getLong(Keys.READEDID, -1);
		searchQuery = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_SEARCHQUERY);
		if(searchQuery != null){
			getActionBar().setTitle(searchQuery);
			getActionBar().setIcon(android.R.drawable.ic_menu_search);
		}

		db = new TagSQLiteHelper(this).getWritableDatabase();

		if(!pref.getBoolean(Keys.TAGSAVED, false)){
			Intent i = new Intent(MainActivity.this, SaveTagActivity.class);
			i.putExtra(SaveTagActivity.INTENT_EXTRA_STARTMAIN, true);
			startActivity(i);
			finish();
			return;
		}else{
			loadPosts(false);
		}
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
					if(searchQuery == null)
						return yandere.getPosts(yanderePage);
					else
						return yandere.searchPosts(searchQuery, yanderePage);
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
					Toast.makeText(MainActivity.this, getString(R.string.get_error), Toast.LENGTH_LONG).show();
					return;
				}
				adapter.addAll(result, readedId);
				Post load = new Post(null, null, null, -1, -1, -1, null, null, null, null, null,
						"LOADMORE", null, null, null, null, false, false, false, false, false, false, -1, -1, -1);
				adapter.add(load, -1);
				if(yanderePage == 1 && searchQuery == null)
					pref.edit().putLong(Keys.READEDID, result[0].getId()).commit();
				yanderePage++;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void onRefresh(){
		loadPosts(true);
	}

	public OnCardClickListener getCardClickListener(){
		return new OnCardClickListener(){

			@Override
			public void onClick(Card c, View view){
				final Post post = ((PostCard)c).getPost();
				if(post.getMD5().equals("LOADMORE")){
					adapter.remove(c);
					loadPosts(false);
					return;
				}

				String fileSize = " (";
				if(howView == SAMPLE)
					fileSize += getFileMB(post.getSample().getSize()) + ")";
				else if(howView == FULL)
					fileSize += getFileMB(post.getFile().getSize()) + ")";

				IconItem[] items;
				if(twitter == null)
					items = new IconItem[5];
				else
					items = new IconItem[6];
				items[0] = new IconItem(howViewStr == null ? getString(R.string.open) : howViewStr + fileSize, android.R.drawable.ic_menu_gallery);
				items[1] = new IconItem(getString(R.string.open_full_size_on_browser), android.R.drawable.ic_menu_set_as);
				items[2] = new IconItem(getString(R.string.save_full_size), android.R.drawable.ic_menu_save);
				items[3] = new IconItem(getString(R.string.share), android.R.drawable.ic_menu_share);
				if(twitter == null){
					items[4] = new IconItem(getString(R.string.detail), android.R.drawable.ic_menu_info_details);
				}else{
					items[4] = new IconItem(getString(R.string.share_on_twitter), R.drawable.twitter_social_icon_blue);
					items[5] = new IconItem(getString(R.string.detail), android.R.drawable.ic_menu_info_details);
				}

				IconDialog dialog = new IconDialog(MainActivity.this);
				dialog.setItems(items, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which){
						final Intent i;
						switch(which){
						case 0:
							i = new Intent(MainActivity.this, ShowImage.class);
							if(howViewStr == null){
								String sampleSize = " (" + getFileMB(post.getSample().getSize()) + ")";
								String fullSize = " (" + getFileMB(post.getFile().getSize()) + ")";
								new AlertDialog.Builder(MainActivity.this)
								.setItems(new String[]{getString(R.string.open_sample_size) + sampleSize,
										getString(R.string.open_full_size) + fullSize}, new OnClickListener(){

									@Override
									public void onClick(DialogInterface dialog, int which){
										if(which == 0){
											i.putExtra(ShowImage.INTENT_EXTRA_URL, post.getSample().getUrl());
											i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.getSample().getSize());
										}else if(which == 1){
											i.putExtra(ShowImage.INTENT_EXTRA_URL, post.getFile().getUrl());
											i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.getFile().getSize());
										}
										startActivity(i);
									}
								}).show();
							}else{
								if(howView == SAMPLE){
									i.putExtra(ShowImage.INTENT_EXTRA_URL, post.getSample().getUrl());
									i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.getSample().getSize());
								}else if(howView == FULL){
									i.putExtra(ShowImage.INTENT_EXTRA_URL, post.getFile().getUrl());
									i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.getFile().getSize());
								}
								startActivity(i);
							}
							break;
						case 1:
							i = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getFile().getUrl()));
							startActivity(i);
							break;
						case 2:
							app.saveImage(MainActivity.this, post);
							break;
						case 3:
							i = new Intent();
							i.setAction(Intent.ACTION_SEND);
							i.setType("text/plain");
							i.putExtra(Intent.EXTRA_TEXT, yandere.getShareText(post));
							startActivity(i);
							break;
						case 4:
							if(twitter == null){
								detail(post);
								return;
							}
							i = new Intent(MainActivity.this, TweetActivity.class);
							i.putExtra(TweetActivity.INTENT_EXTRA_POST, post);
							startActivity(i);
							break;
						case 5:
							detail(post);
							break;
						}
					}
				}).show();
			}
			private void detail(Post post){
				Intent i = new Intent(MainActivity.this, PostDetail.class);
				i.putExtra(PostDetail.INTENT_EXTRA_POSTDATA, post);
				startActivity(i);
			}
		};
	}

	@Override
	public void onResume(){
		super.onResume();
		loadSettings();
		if(app.getClearedHistory() && searchQuery == null){
			invalidateOptionsMenu();
			app.setClearedHistory(false);
		}
	}

	public void loadSettings(){
		switch(pref.getString(Keys.HOWVIEW, Keys.VAL_FULL)){
		case Keys.VAL_SAMPLE:
			howView = SAMPLE;
			howViewStr = getString(R.string.view_sample_size);
			break;
		case Keys.VAL_FULL:
			howView = FULL;
			howViewStr = getString(R.string.view_full_size);
			break;
		case Keys.VAL_ASK:
			howView = ASK;
			howViewStr = null;
			break;
		}

		if(!pref.getString(Keys.TWITTER_USERNAME, "").equals("")){
			Configuration conf = new ConfigurationBuilder()
					.setOAuthConsumerKey(getString(R.string.twitter_ck))
					.setOAuthConsumerSecret(getString(R.string.twitter_cs))
			.build();
			AccessToken at = new AccessToken(pref.getString(Keys.TWITTER_AT, null), pref.getString(Keys.TWITTER_ATS, null));
			twitter = new TwitterFactory(conf).getInstance(at);
		}else{
			twitter = null;
		}
		yandere.setRequestPostCount(pref.getInt(Keys.REQUEST_POSTCOUNT, 50));
	}

	public String getFileMB(int filesize){
		DecimalFormat df = new DecimalFormat("#.#");
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		return df.format((double)filesize / 1024 / 1024) + "MB";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(searchQuery == null){
			getMenuInflater().inflate(R.menu.menu_both, menu);
			final MultiAutoCompleteTextView mactv =
					(MultiAutoCompleteTextView)((View)menu.findItem(R.id.search_view).getActionView()).findViewById(R.id.cactv);
			mactv.setEnabled(false);
			new AsyncTask<Void, Void, ArrayList<String>>(){

				@Override
				protected ArrayList<String> doInBackground(Void... params){
					return new DBUtils(db).loadTagNamesAsArrayList();
				}

				@Override
				protected void onPostExecute(ArrayList<String> result){
					prepareSuggest(result, mactv);
					mactv.setEnabled(true);
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}else{
			getMenuInflater().inflate(R.menu.menu_settings, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	public void prepareSuggest(final ArrayList<String> tags, final MultiAutoCompleteTextView mactv){
		final SuggestAdapter suggestAdapter = new SuggestAdapter(this);

		String[] searchHistory = pref.getString(Keys.SEARCH_HISTORY, "").split(",");
		for(String s : searchHistory)
			suggestAdapter.add(new SearchItem(s, SearchItem.HISTORY));

		for(int i = 0; i < tags.size(); i++)
			suggestAdapter.add(new SearchItem(tags.get(i), SearchItem.TAG));

		mactv.setAdapter(suggestAdapter);
		mactv.setHint("Search post from tag");
		mactv.setTokenizer(new SpaceTokenizer());
		mactv.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
				if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)){
					String query = mactv.getText().toString();
					query = query.replaceAll("\\s+$", "");

					ArrayList<String> history = new ArrayList<String>();
					for(String s : pref.getString(Keys.SEARCH_HISTORY, "").split(",", 0)){
						if(!s.isEmpty())
							history.add(s);
					}
					if(history.indexOf(query) == -1 && tags.indexOf(query) == -1){
						history.add(query);
						String result = "";
						for(String s : history)
							result += s + ",";
						pref.edit().putString(Keys.SEARCH_HISTORY, result).commit();
						suggestAdapter.add(new SearchItem(query, SearchItem.HISTORY));
					}

					Intent i = new Intent(getApplicationContext(), MainActivity.class);
					i.putExtra(MainActivity.INTENT_EXTRA_SEARCHQUERY, query);
					startActivity(i);
				}
				return false;
			}
		});
		mactv.addOnAttachStateChangeListener(new OnAttachStateChangeListener(){
			@Override
			public void onViewDetachedFromWindow(View v){
			}

			@Override
			public void onViewAttachedToWindow(View v){
				InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.showSoftInput(v, 0);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getOrder() == Menu.FIRST + 1)
			startActivity(new Intent(this, Settings.class));
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		db.close();
	}
}
