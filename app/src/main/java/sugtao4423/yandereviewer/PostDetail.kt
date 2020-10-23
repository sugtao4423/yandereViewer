package sugtao4423.yandereviewer

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Spanned
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yandere4j.Post
import yandere4j.Yandere4j
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

class PostDetail : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_POSTDATA = "postdata"
        const val INTENT_EXTRA_ONINTENT = "onIntent"
    }

    private lateinit var post: Post
    private var onIntent = false

    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_detail)
        text = findViewById(R.id.postDetail_text)
        post = intent.getSerializableExtra(INTENT_EXTRA_POSTDATA) as Post
        onIntent = intent.getBooleanExtra(INTENT_EXTRA_ONINTENT, false)

        val tags = post.tags.joinToString(" ")

        supportActionBar?.title = tags
        setActionbarIcon()

        val urlMatcher = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?").matcher(post.source)
        val source = if (urlMatcher.find()) {
            val pixivMatcher = Pattern.compile(
                    "http(s)?://(i[0-9].pixiv|i.pximg).net/img-original/img/[0-9]{4}/([0-9]{2}/){5}([0-9]+)_p[0-9]+\\..+"
            )
                    .matcher(post.source)
            if (pixivMatcher.find()) {
                "https://www.pixiv.net/artworks/" + pixivMatcher.group(4)
            } else {
                post.source
            }
        } else {
            "https://google.com/search?q=${post.source}"
        }

        val date = DateFormat.getDateFormat(applicationContext).format(post.createdAt)

        val str = "<p><strong>Statistics</strong><br>" +
                "Id: ${post.id}<br>" +
                "Posted: $date " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(post.createdAt) +
                " by ${post.author}<br>" +
                "Tags: $tags<br>" +
                "Size: ${post.file.width}x${post.file.height}<br>" +
                "Source: <a href=\"$source\">${post.source}</a><br>" +
                "Rating: ${post.rating}<br>" +
                "Score: ${post.score}<br><br>" +
                "<strong>Preview</strong><br>" +
                "URL: <a href=\"${post.preview.url}\">${post.preview.url}</a><br>" +
                "Size: ${post.preview.width}x${post.preview.height}<br><br>" +
                "<strong>Sample</strong><br>" +
                "URL: <a href=\"${post.sample.url}\">${post.sample.url}</a><br>" +
                "Size: ${post.sample.width}x${post.sample.height}<br>" +
                "File Size: " + App.getFileMB(post.sample.size) + "<br><br>" +
                "<strong>File</strong><br>" +
                "URL: <a href=\"${post.file.url}\">${post.file.url}</a><br>" +
                "Size: ${post.file.width}x${post.file.height}<br>" +
                "File Size: " + App.getFileMB(post.file.size) + "</p>"
        text.text = fromHtml(str)
        text.movementMethod = MutableLinkMovementMethod().apply {
            setOnUrlClickListener(object : MutableLinkMovementMethod.OnUrlClickListener {
                override fun onUrlClick(widget: TextView, uri: Uri) {
                    ChromeIntent(this@PostDetail, uri)
                }
            })
        }
    }

    @Suppress("DEPRECATION")
    private fun fromHtml(source: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(source)
        }
    }

    private fun setActionbarIcon() {
        val actionBar = supportActionBar ?: return
        actionBar.apply {
            setDisplayUseLogoEnabled(true)
            setDisplayShowHomeEnabled(true)
            setIcon(R.drawable.ic_action_refresh)
        }

        val path = applicationContext.cacheDir.absolutePath + "/web_image_cache/" +
                post.preview.url.replace(Regex("[.:/,%?&=]"), "+").replace(Regex("[+]+"), "+")
        val img = File(path)
        if (img.exists()) {
            val d = Drawable.createFromPath(path)
            actionBar.setIcon(d)
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val conn = URL(post.preview.url).openConnection() as HttpsURLConnection
                    conn.setRequestProperty("User-Agent", Yandere4j.USER_AGENT)
                    conn.connect()
                    val inputStream = conn.inputStream
                    val bmp = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    conn.disconnect()
                    BitmapDrawable(resources, bmp)
                } catch (e: IOException) {
                    null
                }
            }
            if (result != null) {
                actionBar.setIcon(result)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.share)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        if (onIntent) {
            menu?.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, R.string.open_sample_size)
                    ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu?.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, R.string.open_full_size)
                    ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu?.add(Menu.NONE, Menu.FIRST + 3, Menu.NONE, R.string.save_full_size)
                    ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            Menu.FIRST -> {
                val i = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, Yandere4j().getShareText(post))
                }
                startActivity(i)
            }
            Menu.FIRST + 1 -> {
                val i = Intent(this, ShowImage::class.java).apply {
                    putExtra(ShowImage.INTENT_EXTRA_URL, post.sample.url)
                    putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.sample.size)
                }
                startActivity(i)
            }
            Menu.FIRST + 2 -> {
                val i = Intent(this, ShowImage::class.java).apply {
                    putExtra(ShowImage.INTENT_EXTRA_URL, post.file.url)
                    putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.file.size)
                }
                startActivity(i)
            }
            Menu.FIRST + 3 -> {
                (applicationContext as App).saveImage(this, post)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}