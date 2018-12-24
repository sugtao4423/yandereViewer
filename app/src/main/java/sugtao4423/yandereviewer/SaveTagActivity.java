package sugtao4423.yandereviewer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;

import java.io.IOException;

import yandere4j.Yandere4j;
import yandere4j.data.Tag;

public class SaveTagActivity extends AppCompatActivity{

    public static final String INTENT_EXTRA_STARTMAIN = "startMain";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final boolean start_main = getIntent().getBooleanExtra(INTENT_EXTRA_STARTMAIN, false);

        final Yandere4j yandere = new Yandere4j();
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        new AsyncTask<Void, Void, Tag[]>(){
            private ProgressDialog progDialog;

            @Override
            protected void onPreExecute(){
                progDialog = new ProgressDialog(SaveTagActivity.this);
                progDialog.setMessage("Loading all tags...\nWait a minute.");
                progDialog.setIndeterminate(false);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setCancelable(false);
                progDialog.show();
            }

            @Override
            protected Tag[] doInBackground(Void... params){
                try{
                    return yandere.getTags(true);
                }catch(JSONException | IOException e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Tag[] tags){
                SQLiteDatabase db = new TagSQLiteHelper(getApplicationContext()).getWritableDatabase();
                new DBUtils(db).writeTags(tags);
                db.close();
                progDialog.dismiss();
                pref.edit().putBoolean(Keys.TAGSAVED, true).commit();
                if(start_main)
                    startActivity(new Intent(SaveTagActivity.this, MainActivity.class));
                finish();
            }
        }.execute();
    }
}
