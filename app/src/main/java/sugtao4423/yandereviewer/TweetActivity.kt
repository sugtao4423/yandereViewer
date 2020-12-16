package sugtao4423.yandereviewer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.tweet_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder
import yandere4j.Post
import yandere4j.Yandere4j

class TweetActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_POST = "post"
    }

    private lateinit var post: Post
    private lateinit var twitter: Twitter

    private lateinit var yandere: Yandere4j

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tweet_activity)
        post = intent.getSerializableExtra(INTENT_EXTRA_POST) as Post

        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val conf = ConfigurationBuilder().run {
            setOAuthConsumerKey(getString(R.string.twitter_ck))
            setOAuthConsumerSecret(getString(R.string.twitter_cs))
            build()
        }
        val at = AccessToken(pref.getString(Keys.TWITTER_AT, null), pref.getString(Keys.TWITTER_ATS, null))
        twitter = TwitterFactory(conf).getInstance(at)
        yandere = Yandere4j()

        addTextWatcher()
        tweetAccount.text = pref.getString(Keys.TWITTER_USERNAME, null)

        tweetText.setText(yandere.getShareText(post))
    }

    fun tweet(@Suppress("UNUSED_PARAMETER") v: View) {
        tweetButton.isEnabled = false
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    twitter.updateStatus(tweetText.text.toString())
                } catch (e: TwitterException) {
                    null
                }
            }
            if (result == null) {
                Toast.makeText(this@TweetActivity, R.string.tweet_failed, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@TweetActivity, R.string.tweet_success, Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }

    private fun addTextWatcher() {
        tweetText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.length?.let {
                    text140.text = (140 - it).toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (tweetText.text.toString().length > 140) {
            menu?.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.undo)?.apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
            menu?.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, R.string.shorten)?.apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            Menu.FIRST -> {
                tweetText.setText(yandere.getShareText(post))
            }
            Menu.FIRST + 1 -> {
                val text = tweetText.text.toString()
                var shortenTitle = yandere.getShareTitle(post)
                val otherLetterLength = text.length - shortenTitle.length - 1
                shortenTitle = shortenTitle.substring(0, 140 - otherLetterLength - 4) + "..."
                tweetText.setText(text.replace(yandere.getShareTitle(post), shortenTitle))
            }
        }
        return super.onOptionsItemSelected(item)
    }

}