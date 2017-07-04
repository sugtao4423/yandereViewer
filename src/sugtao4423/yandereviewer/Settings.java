package sugtao4423.yandereviewer;

import java.io.File;
import java.text.DecimalFormat;

import com.loopj.android.image.WebImageCache;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
		getActionBar().setTitle(getString(R.string.settings));
	}

	public class MyPreferencesFragment extends PreferenceFragment{
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);

			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			final ListPreference how_view = (ListPreference)findPreference("how_view");
			how_view.setSummary(getHowViewSummary(pref.getString("how_view", "full")));
			how_view.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue){
					how_view.setSummary(getHowViewSummary((String)newValue));
					return true;
				}
			});

			Preference twitter = findPreference("twitter");
			final String username = pref.getString("twitter_username", "");
			if(username.equals("")){
				twitter.setTitle(getString(R.string.cooperate_with_twitter));
			}else{
				twitter.setTitle(getString(R.string.cancel_collaboration));
				twitter.setSummary(username);
			}
			twitter.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				
				@Override
				public boolean onPreferenceClick(Preference preference){
					if(username.equals("")){
						startActivity(new Intent(getActivity(), TwitterOAuth.class));
						finish();
					}else{
						new AlertDialog.Builder(getActivity())
						.setTitle(getString(R.string.cancel_collaboration))
						.setMessage(getString(R.string.cancel_collaboration_really))
						.setPositiveButton("OK", new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog, int which){
								pref.edit()
								.putString("twitter_username", "")
								.putString("twitter_at", "")
								.putString("twitter_ats", "")
								.commit();
								Toast.makeText(getActivity(), getString(R.string.cancelled_collaboration), Toast.LENGTH_SHORT).show();
								finish();
							}
						})
						.setNegativeButton("Cancell", null)
						.show();
					}
					return true;
				}
			});

			final Preference changeSaveDir = findPreference("changeSaveDir");
			final String defaultDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
					Environment.DIRECTORY_DOWNLOADS + "/";
			changeSaveDir.setSummary(pref.getString("saveDir", defaultDir));
			changeSaveDir.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					final EditText dirText = new EditText(getActivity());
					String currentDir = pref.getString("saveDir", defaultDir);
					dirText.setText(currentDir);

					new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.changeSaveDir))
					.setView(dirText)
					.setNegativeButton("Cancel", null)
					.setNeutralButton("Default", new OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which){
							if(pref.edit().putString("saveDir", defaultDir).commit())
								changeSaveDir.setSummary(defaultDir);
						}
					})
					.setPositiveButton("OK", new OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which){
							String current = dirText.getText().toString();
							if(!current.endsWith("/"))
								current += "/";
							File fDir = new File(current);
							if(!fDir.exists())
								fDir.mkdirs();
							if(pref.edit().putString("saveDir", current).commit())
								changeSaveDir.setSummary(current);
						}
					}).show();
					return true;
				}
			});

			Preference clearHistory = findPreference("clearHistory");
			clearHistory.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.history_clear))
					.setMessage(getString(R.string.cancel_collaboration_really))
					.setNegativeButton("Cancel", null)
					.setPositiveButton("OK", new OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which){
							SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
							((App)getApplicationContext()).setClearedHistory(pref.edit().remove("searchHistory").commit());
							Toast.makeText(getActivity(), getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
						}
					}).show();
					return true;
				}
			});

			Preference clearCache = findPreference("clearCache");
			clearCache.setSummary(getString(R.string.cache, getCacheSize()));
			clearCache.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					new WebImageCache(Settings.this).clear();
					preference.setSummary(getString(R.string.cache, getCacheSize()));
					Toast.makeText(Settings.this, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		}

		public String getCacheSize(){
			DecimalFormat df = new DecimalFormat("#.#");
			df.setMinimumFractionDigits(2);
			df.setMaximumFractionDigits(2);
			return df.format((double)new WebImageCache(Settings.this).getCacheSize() / 1024 / 1024) + "MB";
		}

		public String getHowViewSummary(String str){
			switch(str){
			case "sample":
				return getString(R.string.sample_size);
			case "full":
				return getString(R.string.full_size);
			case "ask":
				return getString(R.string.ask);
			}
			return null;
		}
	}
}
