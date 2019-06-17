package sugtao4423.yandereviewer

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
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
        object : AsyncTask<Unit, Unit, Boolean>() {
            private lateinit var progressDialog: ProgressDialog

            override fun onPreExecute() {
                progressDialog = ProgressDialog(this@TwitterOAuth).apply {
                    setMessage("Loading...")
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(true)
                    setCanceledOnTouchOutside(false)
                    show()
                }
            }

            override fun doInBackground(vararg params: Unit?): Boolean {
                val ck = getString(R.string.twitter_ck)
                val cs = getString(R.string.twitter_cs)
                val conf = ConfigurationBuilder().run {
                    setOAuthConsumerKey(ck)
                    setOAuthConsumerSecret(cs)
                    build()
                }
                twitter = TwitterFactory(conf).instance
                return try {
                    rt = twitter.getOAuthRequestToken(CALLBACK_URL)
                    true
                } catch (e: TwitterException) {
                    false
                }
            }

            override fun onPostExecute(result: Boolean) {
                progressDialog.dismiss()
                if (result) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rt.authenticationURL))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@TwitterOAuth, R.string.acquisition_of_request_token_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }.execute()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data == null || !intent.data!!.toString().startsWith(CALLBACK_URL)) {
            return
        }

        val verifier = intent.data!!.getQueryParameter("oauth_verifier")

        object : AsyncTask<Unit, Unit, AccessToken?>() {
            private lateinit var progressDialog: ProgressDialog

            override fun onPreExecute() {
                progressDialog = ProgressDialog(this@TwitterOAuth).apply {
                    setMessage("Loading...")
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(true)
                    setCanceledOnTouchOutside(false)
                    show()
                }
            }

            override fun doInBackground(vararg params: Unit?): AccessToken? {
                return try {
                    twitter.getOAuthAccessToken(rt, verifier)
                } catch (e: TwitterException) {
                    null
                }
            }

            override fun onPostExecute(result: AccessToken?) {
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
        }.execute()
    }

}