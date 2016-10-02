package com.tao.yandereviewer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import yandere4j.data.Post;

public class PostDetail extends Activity{

	private Post post;

	private TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_detail);
		text = (TextView)findViewById(R.id.postDetail_text);
		post = (Post)getIntent().getSerializableExtra("postdata");

		String str = "<p><strong>Statistics</strong><br />" +
				"Id: " + post.getId() + "<br />" +
				"Posted: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(post.getCreatedAt()) +
							" by " + post.getAuthor() + "<br />" +
				"Size: " + post.getFile().getWidth() + "x" + post.getFile().getHeight() + "<br />" +
				"Source: <a href=\"" + post.getSource() + "\">" + post.getSource() + "</a><br />" +
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
		text.setText(Html.fromHtml(str));
		text.setMovementMethod(LinkMovementMethod.getInstance());
	}

	public String getMB(int bytesize){
		DecimalFormat df = new DecimalFormat("#.#");
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		return df.format((float)bytesize / 1024 / 1024) + "MB";
	}
}
