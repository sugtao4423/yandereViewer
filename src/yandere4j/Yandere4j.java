package yandere4j;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yandere4j.data.File;
import yandere4j.data.Post;
import yandere4j.data.Preview;
import yandere4j.data.Sample;

public class Yandere4j{

	private static String BASE_URL = "https://yande.re";

	public Post[] getPosts(int page) throws KeyManagementException, NoSuchAlgorithmException, JSONException, IOException{
		return getPosts(getServer(BASE_URL + "/post.json?page=" + page));
	}

	private Post[] getPosts(String json) throws JSONException{
		JSONArray arr = new JSONArray(json);
		Post[] result = new Post[arr.length()];
		for(int i = 0; i < arr.length(); i++)
			result[i] = getPost(arr.getJSONObject(i));
		return result;
	}

	private Post getPost(JSONObject obj) throws JSONException{
		// File
		String url = obj.getString("file_url");
		String ext = obj.getString("file_ext");
		int size = obj.getInt("file_size");
		int width = obj.getInt("width");
		int height = obj.getInt("height");
		File file = new File(url, ext, size, width, height);

		// Preview
		url = obj.getString("preview_url");
		width = obj.getInt("preview_width");
		height = obj.getInt("preview_height");
		Preview preview = new Preview(url, width, height);

		// Sample
		url = obj.getString("sample_url");
		size = obj.getInt("sample_file_size");
		width = obj.getInt("sample_width");
		height = obj.getInt("sample_height");
		Sample sample = new Sample(url, size, width, height);

		// Other
		long id = obj.getLong("id");
		long parent_id = obj.isNull("parent_id") ? -1 : obj.getLong("parent_id");
		long change = obj.getLong("change");

		String[] tags = obj.getString("tags").split(" ");

		String creator_id = obj.getString("creator_id");
		String approver_id = obj.getString("approver_id");
		String author = obj.getString("author");
		String source = obj.getString("source");
		String md5 = obj.getString("md5");
		String rating = obj.getString("rating");
		String status = obj.getString("status");

		Date created_at = new Date(obj.getLong("created_at") * 1000);
		Date updated_at = new Date(obj.getLong("updated_at") * 1000);

		boolean is_shown_in_index = obj.getBoolean("is_shown_in_index");
		boolean is_rating_locked = obj.getBoolean("is_rating_locked");
		boolean has_children = obj.getBoolean("has_children");
		boolean is_pending = obj.getBoolean("is_pending");
		boolean is_held = obj.getBoolean("is_held");
		boolean is_note_locked = obj.getBoolean("is_note_locked");

		int score = obj.getInt("score");
		int last_noted_at = obj.getInt("last_noted_at");
		int last_commented_at = obj.getInt("last_commented_at");

		return new Post(file, preview, sample,
				id, parent_id, change,
				tags,
				creator_id, approver_id, author, source, md5, rating, status,
				created_at, updated_at,
				is_shown_in_index, is_rating_locked, has_children, is_pending, is_held, is_note_locked,
				score, last_noted_at, last_commented_at);
	}

	public String getServer(String url) throws ParseException, ClientProtocolException, IOException{
		HttpGet httpGet = new HttpGet(url);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String result = EntityUtils.toString(httpClient.execute(httpGet).getEntity(), "UTF-8");
		httpClient.getConnectionManager().shutdown();
		return result;
	}
}
