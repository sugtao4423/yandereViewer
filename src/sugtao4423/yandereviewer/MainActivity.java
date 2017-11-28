package sugtao4423.yandereviewer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import sugtao4423.icondialog.IconDialog;
import sugtao4423.icondialog.IconItem;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class MainActivity extends AppCompatActivity implements OnRefreshListener{

	public static final String INTENT_EXTRA_SEARCHQUERY = "searchQuery";

	private static final int SAMPLE = 0;
	private static final int FULL = 1;
	private static final int ASK = 2;

	private App app;

	private PostGridView grid;
	private ActionMode multiSelectMode;
	private HashMap<Post, View> multiSelectItems;
	private SwipeRefreshLayout swipeRefresh;
	private PostAdapter adapter;
	private Yandere4j yandere;
	private int yanderePage;
	private String searchQuery;

	private SharedPreferences pref;
	private int howView;
	private SQLiteDatabase db;

	private Twitter twitter;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
		grid = (PostGridView)findViewById(R.id.grid);
		adapter = new PostAdapter(this);
		grid.setAdapter(adapter);

		multiSelectItems = new HashMap<Post, View>();

		swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
		swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeRefresh.setOnRefreshListener(this);

		app = (App)getApplicationContext();
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		yandere = new Yandere4j();

		loadSettings();
		yanderePage = 1;
		searchQuery = getIntent().getStringExtra(MainActivity.INTENT_EXTRA_SEARCHQUERY);
		if(searchQuery != null){
			getSupportActionBar().setTitle(searchQuery);
			getSupportActionBar().setIcon(android.R.drawable.ic_menu_search);
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

			@Override
			protected void onPreExecute(){
				swipeRefresh.setRefreshing(true);
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
				swipeRefresh.setRefreshing(false);
				if(result == null){
					Toast.makeText(MainActivity.this, getString(R.string.get_error), Toast.LENGTH_LONG).show();
					return;
				}
				adapter.addAll(result);
				if(result.length >= yandere.getRequestPostCount()){
					Post load = new Post(null, null, null, -1, -1, -1, null, null, null, null, null,
							"LOADMORE", null, null, null, null, false, false, false, false, false, false, -1, -1, -1);
					adapter.add(load);
				}
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

	public OnClickListener getOnCardClickListener(final Post post){
		return new OnClickListener(){

			@Override
			public void onClick(View view){
				if(post.getMD5().equals("LOADMORE")){
					adapter.remove(post);
					loadPosts(false);
					return;
				}
				if(multiSelectMode != null){
					if(!view.isSelected()){
						multiSelectItems.put(post, view);
						view.setSelected(true);
						view.setBackgroundColor(Color.parseColor("#B3E5FC"));
					}else{
						multiSelectItems.remove(post);
						view.setSelected(false);
						view.setBackgroundColor(((CardView)view).getCardBackgroundColor().getDefaultColor());
					}
					int selectedCount = multiSelectItems.size();
					multiSelectMode.setTitle(selectedCount + " selected");
					if(selectedCount == 0)
						multiSelectMode.finish();
					return;
				}

				String openText = null;
				switch(howView){
				case ASK:
					openText = getString(R.string.open);
					break;
				case SAMPLE:
					openText = getString(R.string.view_sample_size) + " (" + getFileMB(post.getSample().getSize()) + ")";
					break;
				case FULL:
					openText = getString(R.string.view_full_size) + " (" + getFileMB(post.getFile().getSize()) + ")";
					break;
				}

				IconItem[] items = new IconItem[(twitter == null) ? 5 : 6];
				items[0] = new IconItem(openText, android.R.drawable.ic_menu_gallery);
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
				dialog.setItems(items, new android.content.DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which){
						final Intent i;
						switch(which){
						case 0:
							i = new Intent(MainActivity.this, ShowImage.class);
							if(howView == ASK){
								String sampleSize = " (" + getFileMB(post.getSample().getSize()) + ")";
								String fullSize = " (" + getFileMB(post.getFile().getSize()) + ")";
								new AlertDialog.Builder(MainActivity.this)
								.setItems(new String[]{getString(R.string.open_sample_size) + sampleSize,
										getString(R.string.open_full_size) + fullSize}, new android.content.DialogInterface.OnClickListener(){

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
							new ChromeIntent(MainActivity.this, post.getFile().getUrl());
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

	public OnLongClickListener getOnCardLongClickListener(){
		return new OnLongClickListener(){

			@Override
			public boolean onLongClick(final View v){
				startSupportActionMode(new ActionMode.Callback(){

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu){
						multiSelectMode = mode;
						menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Save All").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
						v.callOnClick();
						return true;
					}

					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu){
						return false;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item){
						if(item.getItemId() == Menu.FIRST){
							app.saveImages(MainActivity.this, multiSelectItems.keySet().toArray(new Post[multiSelectItems.size()]));
							mode.finish();
						}
						return true;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode){
						multiSelectMode = null;
						for(View v : multiSelectItems.values()){
							v.setSelected(false);
							v.setBackgroundColor(((CardView)v).getCardBackgroundColor().getDefaultColor());
						}
						multiSelectItems.clear();
					}
				});
				return true;
			}
		};
	}

	@Override
	public void onResume(){
		super.onResume();
		loadSettings();
		if(searchQuery == null && (app.getClearedHistory() || app.getIsRefreshTags())){
			invalidateOptionsMenu();
			app.setClearedHistory(false);
			app.setIsRefreshTags(false);
		}
	}

	public void loadSettings(){
		switch(pref.getString(Keys.HOWVIEW, Keys.VAL_FULL)){
		case Keys.VAL_SAMPLE:
			howView = SAMPLE;
			break;
		case Keys.VAL_FULL:
			howView = FULL;
			break;
		case Keys.VAL_ASK:
			howView = ASK;
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
			new MenuInflater(this).inflate(R.menu.menu_both, menu);
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
			new MenuInflater(this).inflate(R.menu.menu_settings, menu);
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
