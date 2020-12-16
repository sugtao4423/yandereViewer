package sugtao4423.yandereviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import yandere4j.Post

class RequestPermissionActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_GRANTED_SAVE_POSTS = "grantedSavePosts"
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 810
    }

    private lateinit var savePosts: Array<Post>

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savePosts = intent.getSerializableExtra(INTENT_EXTRA_GRANTED_SAVE_POSTS) as Array<Post>
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            return
        }
        if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            (applicationContext as App).saveImages(this, savePosts)
        } else {
            Toast.makeText(applicationContext, R.string.permission_rejected, Toast.LENGTH_LONG).show()
        }
        finish()
    }

}