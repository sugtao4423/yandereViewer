package sugtao4423.yandereviewer

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import yandere4j.Post
import yandere4j.Yandere4j
import java.util.regex.Pattern

class IntentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val yandere = Yandere4j()
        if (intent.action != Intent.ACTION_VIEW || intent.data == null) {
            finish()
            return
        }
        val url = intent.data!!.toString()
        val matcher = Pattern.compile("http(s)?://yande.re/post/show/([0-9]+)").matcher(url)
        if (matcher.find()) {
            val id = matcher.group(2).toLong()
            object : AsyncTask<Unit, Unit, Post?>() {
                private lateinit var progressDialog: ProgressDialog

                override fun onPreExecute() {
                    progressDialog = ProgressDialog(this@IntentActivity).apply {
                        setMessage("Loading...")
                        isIndeterminate = false
                        setProgressStyle(ProgressDialog.STYLE_SPINNER)
                        setCancelable(true)
                        setCanceledOnTouchOutside(false)
                        show()
                    }
                }

                override fun doInBackground(vararg params: Unit?): Post? {
                    return try {
                        yandere.getPost(id)
                    } catch (e: Exception) {
                        null
                    }
                }

                override fun onPostExecute(result: Post?) {
                    progressDialog.dismiss()
                    if (result == null) {
                        Toast.makeText(this@IntentActivity, getString(R.string.failed_acquire_details), Toast.LENGTH_LONG).show()
                        finish()
                        return
                    }
                    val i = Intent(this@IntentActivity, PostDetail::class.java).apply {
                        putExtra(PostDetail.INTENT_EXTRA_POSTDATA, result)
                        putExtra(PostDetail.INTENT_EXTRA_ONINTENT, true)
                    }
                    startActivity(i)
                    finish()
                }
            }.execute()
        } else {
            Toast.makeText(this, getString(R.string.did_not_match_regular_expression), Toast.LENGTH_LONG).show()
            finish()
        }
    }

}
