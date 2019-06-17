package sugtao4423.yandereviewer

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.tenthbit.view.ZoomImageView
import yandere4j.Yandere4j
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ShowImage : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_URL = "url"
        const val INTENT_EXTRA_FILESIZE = "filesize"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val image = ZoomImageView(this)
        setContentView(image)
        val url = intent.getStringExtra(INTENT_EXTRA_URL)
        val size = intent.getIntExtra(INTENT_EXTRA_FILESIZE, -1)

        object : AsyncTask<Unit, Int, Bitmap?>() {
            private lateinit var progressDialog: ProgressDialog

            override fun onPreExecute() {
                progressDialog = ProgressDialog(this@ShowImage).apply {
                    setMessage("Loading...")
                    isIndeterminate = false
                    setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                    max = size
                    progress = 0
                    setCancelable(true)
                    setCanceledOnTouchOutside(false)
                    setOnCancelListener {
                        cancel(true)
                        finish()
                    }
                    show()
                }
            }

            override fun doInBackground(vararg params: Unit?): Bitmap? {
                return try {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.apply {
                        setRequestProperty("User-Agent", Yandere4j.USER_AGENT)
                        connect()
                    }
                    val inputStream = conn.inputStream
                    val bout = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var len = inputStream.read(buffer)
                    var i = 1
                    while (len > 0) {
                        if (isCancelled) {
                            conn.disconnect()
                            inputStream.close()
                            bout.close()
                            break
                        }
                        bout.write(buffer, 0, len)
                        len = inputStream.read(buffer)
                        publishProgress(++i * 1024)
                    }
                    val tmp = bout.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
                    inputStream.close()
                    conn.disconnect()
                    bitmap
                } catch (e: IOException) {
                    null
                }
            }

            override fun onProgressUpdate(vararg values: Int?) {
                progressDialog.progress = values[0]!!
            }

            override fun onPostExecute(result: Bitmap?) {
                progressDialog.dismiss()
                if (result == null) {
                    Toast.makeText(this@ShowImage, R.string.could_not_open, Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
                image.setImageBitmap(result)
            }

            override fun onCancelled() {
                Toast.makeText(this@ShowImage, R.string.cancelled, Toast.LENGTH_SHORT).show()
            }
        }.execute()
    }

}