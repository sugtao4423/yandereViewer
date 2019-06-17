package yandere4j

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*

class Yandere4j {

    companion object {
        const val USER_AGENT = "yande.re viewer https://github.com/sugtao4423/yandereViewer"
        private const val BASE_URL = "https://yande.re"
    }

    var requestPostCount = 50

    @Throws(MalformedURLException::class, JSONException::class, IOException::class)
    fun getPosts(page: Int): Array<Post> {
        return getPosts(getServer("$BASE_URL/post.json?page=$page&limit=$requestPostCount"))
    }

    @Throws(MalformedURLException::class, IOException::class, JSONException::class)
    fun getPost(id: Long): Post {
        return getPost(JSONArray(getServer("$BASE_URL/post.json?tags=id:$id")).getJSONObject(0))
    }

    @Throws(MalformedURLException::class, IOException::class, JSONException::class)
    fun getTags(sortWithId: Boolean): Array<Tag> {
        val tags = getTags(getServer("$BASE_URL/tag.json?limit=0"))
        if (sortWithId) {
            Arrays.sort(tags)
        }
        return tags
    }

    @Throws(MalformedURLException::class, IOException::class, JSONException::class)
    fun searchPosts(query: String, page: Int): Array<Post> {
        val q = URLEncoder.encode(query, "UTF-8")
        return getPosts(getServer("$BASE_URL/post.json?tags=$q&page=$page&limit=$requestPostCount"))
    }

    fun getFileName(post: Post): String {
        val name = "yande.re ${post.id} " + post.tags.joinToString(" ")
        return "$name.${post.file.ext}"
    }

    fun getShareText(post: Post): String {
        return getShareTitle(post) + " | #${post.id} | yande.re " + getShareURL(post)
    }

    fun getShareTitle(post: Post): String {
        return post.tags.joinToString(" ")
    }

    fun getShareURL(post: Post): String {
        return "$BASE_URL/post/show/${post.id}"
    }

    @Throws(JSONException::class)
    private fun getPosts(json: String): Array<Post> {
        val arr = JSONArray(json)
        val result = arrayListOf<Post>()
        for (i in 0 until arr.length()) {
            result.add(getPost(arr.getJSONObject(i)))
        }
        return result.toTypedArray()
    }

    @Throws(JSONException::class)
    private fun getPost(obj: JSONObject): Post {
        val file = obj.run {
            val url = getString("file_url")
            val ext = getString("file_ext")
            val size = getInt("file_size")
            val width = getInt("width")
            val height = getInt("height")
            File(url, ext, size, width, height)
        }

        val preview = obj.run {
            val url = getString("preview_url")
            val width = getInt("preview_width")
            val height = getInt("preview_height")
            Preview(url, width, height)
        }

        val sample = obj.run {
            val url = getString("sample_url")
            val size = getInt("sample_file_size")
            val width = getInt("sample_width")
            val height = getInt("sample_height")
            Sample(url, size, width, height)
        }

        obj.run {
            val id = getLong("id")
            val parentId = if (isNull("parent_id")) -1 else getLong("parent_id")
            val change = getLong("change")

            val tags = getString("tags").split(" ").toTypedArray()

            val creatorId = getString("creator_id")
            val approverId = getString("approver_id")
            val author = getString("author")
            val source = getString("source")
            val md5 = getString("md5")
            val rating = getString("rating")
            val status = getString("status")

            val createdAt = Date(getLong("created_at") * 1000)
            val updatedAt = Date(getLong("updated_at") * 1000)

            val isShownInIndex = getBoolean("is_shown_in_index")
            val isRatingLocked = getBoolean("is_rating_locked")
            val hasChildren = getBoolean("has_children")
            val isPending = getBoolean("is_pending")
            val isHeld = getBoolean("is_held")
            val isNoteLocked = getBoolean("is_note_locked")

            val score = getInt("score")
            val lastNotedAt = getInt("last_noted_at")
            val lastCommentedAt = getInt("last_commented_at")

            return Post(
                file, preview, sample,
                id, parentId, change,
                tags,
                creatorId, approverId, author, source, md5, rating, status,
                createdAt, updatedAt,
                isShownInIndex, isRatingLocked, hasChildren, isPending, isHeld, isNoteLocked,
                score, lastNotedAt, lastCommentedAt
            )
        }
    }

    @Throws(JSONException::class)
    private fun getTags(json: String): Array<Tag> {
        val arr = JSONArray(json)
        val result = arrayListOf<Tag>()
        for (i in 0 until arr.length()) {
            result.add(getTag(arr.getJSONObject(i)))
        }
        return result.toTypedArray()
    }

    @Throws(JSONException::class)
    private fun getTag(obj: JSONObject): Tag {
        obj.run {
            val id = getInt("id")
            val name = getString("name")
            val count = getInt("count")
            val type = getInt("type")
            val ambiguous = getBoolean("ambiguous")
            return Tag(id, name, count, type, ambiguous)
        }
    }

    @Throws(MalformedURLException::class, IOException::class)
    private fun getServer(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.connect()
        val inputStream = conn.inputStream
        val response = BufferedReader(InputStreamReader(inputStream)).readText()
        inputStream.close()
        conn.disconnect()
        return response
    }

}