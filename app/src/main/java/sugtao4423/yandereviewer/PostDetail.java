package sugtao4423.yandereviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sugtao4423.yandereviewer.MutableLinkMovementMethod.OnUrlClickListener;
import yandere4j.Yandere4j;
import yandere4j.data.Post;

public class PostDetail extends AppCompatActivity{

    public static final String INTENT_EXTRA_POSTDATA = "postdata";
    public static final String INTENT_EXTRA_ONINTENT = "onIntent";

    private Post post;
    private boolean onIntent;

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_detail);
        text = (TextView)findViewById(R.id.postDetail_text);
        post = (Post)getIntent().getSerializableExtra(INTENT_EXTRA_POSTDATA);
        onIntent = getIntent().getBooleanExtra(INTENT_EXTRA_ONINTENT, false);

        String tags = "";
        for(String s : post.getTags())
            tags += s + " ";
        tags = tags.substring(0, tags.length() - 1);

        getSupportActionBar().setTitle(tags);
        setActionbarIcon();

        String source;
        Matcher urlMatcher = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?").matcher(post.getSource());
        if(urlMatcher.find()){
            Matcher pixivMatcher = Pattern.compile(
                    "http(s)?://(i[0-9].pixiv|i.pximg).net/img-original/img/[0-9]{4}/([0-9]{2}/){5}([0-9]+)_p[0-9]+\\..+")
                    .matcher(post.getSource());
            source = pixivMatcher.find() ?
                    "http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + pixivMatcher.group(4) : post.getSource();
        }else{
            source = "http://google.com/search?q=" + post.getSource();
        }
        String date = DateFormat.getDateFormat(getApplicationContext()).format(post.getCreatedAt());

        String str = "<p><strong>Statistics</strong><br />" +
                "Id: " + post.getId() + "<br />" +
                "Posted: " + date + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(post.getCreatedAt()) +
                " by " + post.getAuthor() + "<br />" +
                "Tags: " + tags + "<br />" +
                "Size: " + post.getFile().getWidth() + "x" + post.getFile().getHeight() + "<br />" +
                "Source: <a href=\"" + source + "\">" + post.getSource() + "</a><br />" +
                "Rating: " + post.getRating() + "<br />" +
                "Score: " + post.getScore() + "<br /><br />" +
                "<strong>Preview</strong><br />" +
                "URL: <a href=\"" + post.getPreview().getUrl() + "\">" + post.getPreview().getUrl() + "</a><br />" +
                "Size: " + post.getPreview().getWidth() + "x" + post.getPreview().getHeight() + "<br /><br />" +
                "<strong>Sample</strong><br />" +
                "URL: <a href=\"" + post.getSample().getUrl() + "\">" + post.getSample().getUrl() + "</a><br />" +
                "Size: " + post.getSample().getWidth() + "x" + post.getSample().getHeight() + "<br />" +
                "File Size: " + getMB(post.getSample().getSize()) + "<br /><br />" +
                "<strong>File</strong><br />" +
                "URL: <a href=\"" + post.getFile().getUrl() + "\">" + post.getFile().getUrl() + "</a><br />" +
                "Size: " + post.getFile().getWidth() + "x" + post.getFile().getHeight() + "<br />" +
                "File Size: " + getMB(post.getFile().getSize()) + "</p>";
        MutableLinkMovementMethod mlmm = new MutableLinkMovementMethod();
        mlmm.setOnUrlClickListener(new OnUrlClickListener(){
            @Override
            public void onUrlClick(TextView widget, Uri uri){
                new ChromeIntent(PostDetail.this, uri);
            }
        });
        text.setText(fromHtml(str));
        text.setMovementMethod(mlmm);
    }

    @SuppressWarnings("deprecation")
    public Spanned fromHtml(String source){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        else
            return Html.fromHtml(source);
    }

    public String getMB(int bytesize){
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        return df.format((float)bytesize / 1024 / 1024) + "MB";
    }

    public void setActionbarIcon(){
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_action_refresh);
        String path = getApplicationContext().getCacheDir().getAbsolutePath() + "/web_image_cache/" +
                post.getPreview().getUrl().replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        File img = new File(path);
        if(img.exists()){
            Drawable d = Drawable.createFromPath(path);
            actionBar.setIcon(d);
        }else{
            new AsyncTask<Void, Void, Drawable>(){

                @Override
                protected Drawable doInBackground(Void... params){
                    try{
                        HttpURLConnection connection = (HttpURLConnection)new URL(post.getPreview().getUrl()).openConnection();
                        connection.connect();
                        InputStream is = connection.getInputStream();
                        Bitmap bmp = BitmapFactory.decodeStream(is);
                        is.close();
                        connection.disconnect();
                        return new BitmapDrawable(getResources(), bmp);
                    }catch(IOException e){
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Drawable d){
                    if(d != null)
                        actionBar.setIcon(d);
                }
            }.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, getString(R.string.share)).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        if(onIntent){
            menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, getString(R.string.open_sample_size)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, getString(R.string.open_full_size)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(Menu.NONE, Menu.FIRST + 3, Menu.NONE, getString(R.string.save_full_size)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == Menu.FIRST){
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, new Yandere4j().getShareText(post));
            startActivity(i);
        }else if(item.getItemId() == Menu.FIRST + 1){
            Intent i = new Intent(PostDetail.this, ShowImage.class);
            i.putExtra(ShowImage.INTENT_EXTRA_URL, post.getSample().getUrl());
            i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.getSample().getSize());
            startActivity(i);
        }else if(item.getItemId() == Menu.FIRST + 2){
            Intent i = new Intent(PostDetail.this, ShowImage.class);
            i.putExtra(ShowImage.INTENT_EXTRA_URL, post.getFile().getUrl());
            i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.getFile().getSize());
            startActivity(i);
        }else if(item.getItemId() == Menu.FIRST + 3){
            ((App)getApplicationContext()).saveImage(PostDetail.this, post);
        }
        return super.onOptionsItemSelected(item);
    }
}
