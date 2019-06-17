package sugtao4423.yandereviewer

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import yandere4j.Tag
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

        object : AsyncTask<Unit, Unit, Array<Tag>?>() {
            private lateinit var progressDialog: ProgressDialog

            override fun onPreExecute() {
                progressDialog = ProgressDialog(this@SaveTagActivity).apply {
                    setMessage("Loading all tags...\nWait a minute.")
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(false)
                    show()
                }
            }

            override fun doInBackground(vararg params: Unit?): Array<Tag>? {
                return try {
                    yandere.getTags(true)
                } catch (e: Exception) {
                    null
                }
            }

            override fun onPostExecute(result: Array<Tag>?) {
                val db = TagSQLiteHelper(applicationContext).writableDatabase
                DBUtils(db).writeTags(result!!)
                db.close()
                progressDialog.dismiss()
                pref.edit().putBoolean(Keys.TAGSAVED, true).commit()
                if (startMain) {
                    startActivity(Intent(this@SaveTagActivity, MainActivity::class.java))
                }
                finish()
            }
        }.execute()
    }

}