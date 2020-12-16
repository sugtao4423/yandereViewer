package sugtao4423.yandereviewer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tenthbit.view.ZoomImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.support.progressdialog.ProgressDialog
import yandere4j.Yandere4j
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

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

        CoroutineScope(Dispatchers.Main).launch {
            var isCancelled = false
            val progressDialog = ProgressDialog(this@ShowImage).apply {
                setMessage("Loading...")
                isIndeterminate = false
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                max = size / 1024
                progress = 0
                setProgressNumberFormat("%1d/%2d KB")
                setCancelable(true)
                setCanceledOnTouchOutside(false)
                setOnCancelListener {
                    isCancelled = true
                    finish()
                }
                show()
            }
            val result = withContext(Dispatchers.IO) {
                try {
                    val conn = URL(url).openConnection() as HttpsURLConnection
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
                        withContext(Dispatchers.Main) {
                            progressDialog.progress = ++i
                        }
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
            progressDialog.dismiss()
            if (isCancelled) {
                Toast.makeText(this@ShowImage, R.string.cancelled, Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (result == null) {
                Toast.makeText(this@ShowImage, R.string.could_not_open, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            image.setImageBitmap(result)
        }
    }

}