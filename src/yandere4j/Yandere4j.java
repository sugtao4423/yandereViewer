package yandere4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yandere4j.data.File;
import yandere4j.data.Post;
import yandere4j.data.Preview;
import yandere4j.data.Sample;
import yandere4j.data.Tag;

public class Yandere4j{

	private static String BASE_URL = "https://yande.re";

	public Post[] getPosts(int page) throws KeyManagementException, NoSuchAlgorithmException, JSONException, IOException{
		return getPosts(getServer(BASE_URL + "/post.json?page=" + page));
	}

	public Tag[] getTags(boolean sortWithId) throws MalformedURLException, JSONException, IOException{
		Tag[] tags = getTags(getServer(BASE_URL + "/tag.json?limit=0"));
		if(sortWithId)
			Arrays.sort(tags);
		return tags;
	}

	public Post[] searchPosts(String query, int page) throws MalformedURLException, JSONException, IOException{
		return getPosts(getServer(BASE_URL + "/post.json?tags=" + query + "&page=" + page));
	}

	public String getFileName(Post post){
		String name = "yande.re " + post.getId() + " ";
		for(String s : post.getTags())
			name += s + " ";
		name = name.substring(0, name.length() - 1);
		return name + "." + post.getFile().getExt();
	}

	public String getShareText(Post post, boolean onlyURL){
		String url = BASE_URL + "/post/show/" + post.getId();
		if(onlyURL){
			return url;
		}else{
			String tags = "";
			for(String s : post.getTags())
				tags += s + " ";
			tags = tags.substring(0, tags.length() - 1);
			return tags + " " + url;
		}
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

	private Tag[] getTags(String json) throws JSONException{
		JSONArray arr = new JSONArray(json);
		Tag[] result = new Tag[arr.length()];
		for(int i = 0; i < arr.length(); i++)
			result[i] = getTag(arr.getJSONObject(i));
		return result;
	}

	private Tag getTag(JSONObject obj) throws JSONException{
		int id = obj.getInt("id");
		String name = obj.getString("name");
		int count = obj.getInt("count");
		int type = obj.getInt("type");
		boolean ambiguous = obj.getBoolean("ambiguous");

		return new Tag(id, name, count, type, ambiguous);
	}

	public String getServer(String url) throws MalformedURLException, IOException{
		HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");
		conn.connect();
		StringBuffer sb = new StringBuffer();
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while((line = br.readLine()) != null)
			sb.append(line);
		is.close();
		br.close();
		conn.disconnect();
		return sb.toString();
	}
}
