package sugtao4423.yandereviewer;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class IntentActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        final Yandere4j yandere = new Yandere4j();
        if(!getIntent().getAction().equals(Intent.ACTION_VIEW)){
            finish();
            return;
        }
        String url = getIntent().getData().toString();
        Matcher matcher = Pattern.compile("http(s)?://yande.re/post/show/([0-9]+)").matcher(url);
        if(matcher.find()){
            final long id = Long.parseLong(matcher.group(2));
            new AsyncTask<Void, Void, Post>(){
                private ProgressDialog progDialog;

                @Override
                protected void onPreExecute(){
                    progDialog = new ProgressDialog(IntentActivity.this);
                    progDialog.setMessage("Loading...");
                    progDialog.setIndeterminate(false);
                    progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progDialog.setCancelable(true);
                    progDialog.setCanceledOnTouchOutside(false);
                    progDialog.show();
                }

                @Override
                protected Post doInBackground(Void... params){
                    try{
                        return yandere.getPost(id);
                    }catch(IOException | JSONException e){
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Post post){
                    progDialog.dismiss();
                    if(post == null){
                        Toast.makeText(IntentActivity.this, getString(R.string.failed_acquire_details), Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    Intent i = new Intent(IntentActivity.this, PostDetail.class);
                    i.putExtra(PostDetail.INTENT_EXTRA_POSTDATA, post);
                    i.putExtra(PostDetail.INTENT_EXTRA_ONINTENT, true);
                    startActivity(i);
                    finish();
                }
            }.execute();
        }else{
            Toast.makeText(IntentActivity.this, getString(R.string.did_not_match_regular_expression), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
