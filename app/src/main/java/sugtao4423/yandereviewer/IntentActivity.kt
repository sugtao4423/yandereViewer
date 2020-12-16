package sugtao4423.yandereviewer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.support.progressdialog.ProgressDialog
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
            val id = matcher.group(2)!!.toLong()
            CoroutineScope(Dispatchers.Main).launch {
                val progressDialog = ProgressDialog(this@IntentActivity).apply {
                    setMessage("Loading...")
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(true)
                    setCanceledOnTouchOutside(false)
                    show()
                }
                val result = withContext(Dispatchers.IO) {
                    try {
                        yandere.getPost(id)
                    } catch (e: Exception) {
                        null
                    }
                }
                progressDialog.dismiss()
                if (result == null) {
                    Toast.makeText(this@IntentActivity, getString(R.string.failed_acquire_details), Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
                val i = Intent(this@IntentActivity, PostDetail::class.java).apply {
                    putExtra(PostDetail.INTENT_EXTRA_POSTDATA, result)
                    putExtra(PostDetail.INTENT_EXTRA_ONINTENT, true)
                }
                startActivity(i)
                finish()
            }
        } else {
            Toast.makeText(this, getString(R.string.did_not_match_regular_expression), Toast.LENGTH_LONG).show()
            finish()
        }
    }

}
