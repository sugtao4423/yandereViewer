package sugtao4423.yandereviewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.support.progressdialog.ProgressDialog
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationBuilder

class TwitterOAuth : AppCompatActivity() {

    companion object {
        const val CALLBACK_URL = "https://localhost/sugtao4423.yandereviewer/oauth"
    }

    private lateinit var twitter: Twitter
    private lateinit var rt: RequestToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = ProgressDialog(this@TwitterOAuth).apply {
                setMessage("Loading...")
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(true)
                setCanceledOnTouchOutside(false)
                show()
            }
            val result = withContext(Dispatchers.IO) {
                val ck = getString(R.string.twitter_ck)
                val cs = getString(R.string.twitter_cs)
                val conf = ConfigurationBuilder().run {
                    setOAuthConsumerKey(ck)
                    setOAuthConsumerSecret(cs)
                    build()
                }
                twitter = TwitterFactory(conf).instance
                try {
                    rt = twitter.getOAuthRequestToken(CALLBACK_URL)
                    true
                } catch (e: TwitterException) {
                    false
                }
            }
            progressDialog.dismiss()
            if (result) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rt.authenticationURL))
                startActivity(intent)
            } else {
                Toast.makeText(this@TwitterOAuth, R.string.acquisition_of_request_token_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data == null || !intent.data!!.toString().startsWith(CALLBACK_URL)) {
            return
        }

        val verifier = intent.data!!.getQueryParameter("oauth_verifier")

        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = ProgressDialog(this@TwitterOAuth).apply {
                setMessage("Loading...")
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(true)
                setCanceledOnTouchOutside(false)
                show()
            }
            val result = withContext(Dispatchers.IO) {
                try {
                    twitter.getOAuthAccessToken(rt, verifier)
                } catch (e: TwitterException) {
                    null
                }
            }
            progressDialog.dismiss()
            if (result != null) {
                PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().apply {
                    putString(Keys.TWITTER_AT, result.token)
                    putString(Keys.TWITTER_ATS, result.tokenSecret)
                    putString(Keys.TWITTER_USERNAME, "@${result.screenName}")
                    commit()
                }
                Toast.makeText(this@TwitterOAuth, R.string.cooperated, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@TwitterOAuth, R.string.acquisition_of_access_token_failed, Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }

}