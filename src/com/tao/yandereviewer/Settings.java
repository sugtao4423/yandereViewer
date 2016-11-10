package com.tao.yandereviewer;

import java.text.DecimalFormat;

import com.loopj.android.image.WebImageCache;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Settings extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
		getActionBar().setTitle("設定");
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
				twitter.setTitle("Twitterと連携");
			}else{
				twitter.setTitle("連携を解除");
				twitter.setSummary(username);
			}
			twitter.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				
				@Override
				public boolean onPreferenceClick(Preference preference){
					if(username.equals("")){
						startActivity(new Intent(getActivity(), TwitterOAuth.class));
					}else{
						pref.edit()
						.putString("twitter_username", "")
						.putString("twitter_at", "")
						.putString("twitter_ats", "")
						.commit();
						Toast.makeText(getActivity(), "連携を解除しました", Toast.LENGTH_SHORT).show();
					}
					finish();
					return true;
				}
			});

			Preference clearCache = findPreference("clearCache");
			clearCache.setSummary("キャッシュ: " + getCacheSize());
			clearCache.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference preference){
					new WebImageCache(Settings.this).clear();
					preference.setSummary("キャッシュ: " + getCacheSize());
					Toast.makeText(Settings.this, "キャッシュが削除されました", Toast.LENGTH_SHORT).show();
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
				return "サンプルサイズ";
			case "full":
				return "フルサイズ";
			case "ask":
				return "その都度確認";
			}
			return null;
		}
	}
}
