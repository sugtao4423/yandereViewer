package sugtao4423.yandereviewer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.support.progressdialog.ProgressDialog
import yandere4j.Yandere4j

class SaveTagActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_STARTMAIN = "startMain"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startMain = intent.getBooleanExtra(INTENT_EXTRA_STARTMAIN, false)

        val yandere = Yandere4j()
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        CoroutineScope(Dispatchers.Main).launch {
            val progressDialog = ProgressDialog(this@SaveTagActivity).apply {
                setMessage("Loading all tags...\nWait a minute.")
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(false)
                show()
            }
            val result = withContext(Dispatchers.IO) {
                try {
                    yandere.getTags(true)
                } catch (e: Exception) {
                    null
                }
            }
            if (result == null) {
                Toast.makeText(this@SaveTagActivity, R.string.get_tags_failed, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            DBUtils(applicationContext).apply {
                writeTags(result)
                close()
            }
            progressDialog.dismiss()
            pref.edit().putBoolean(Keys.TAGSAVED, true).commit()
            if (startMain) {
                startActivity(Intent(this@SaveTagActivity, MainActivity::class.java))
            }
            finish()
        }
    }

}