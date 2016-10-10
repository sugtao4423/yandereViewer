package com.tao.yandereviewer;

import java.text.DecimalFormat;

import com.loopj.android.image.WebImageCache;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
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
	}
}
