package sugtao4423.yandereviewer

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import yandere4j.Post
import java.text.DecimalFormat

class App : Application() {

    companion object {
        @JvmStatic
        fun getFileMB(fileByteSize: Int): String {
            val df = DecimalFormat("#.#").apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            return df.format(fileByteSize.toFloat() / 1024 / 1024) + "MB"
        }
    }

    var clearedHistory = false
    var isRefreshTags = false

    fun saveImage(context: Context, post: Post) {
        val posts = arrayOf(post)
        saveImages(context, posts)
    }

    fun saveImages(context: Context, posts: Array<Post>) {
        val writeExternalStorage = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (writeExternalStorage != PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(context, RequestPermissionActivity::class.java)
            intent.putExtra(RequestPermissionActivity.INTENT_EXTRA_GRANTED_SAVE_POSTS, posts)
            context.startActivity(intent)
            return
        }

        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra(DownloadService.INTENT_KEY_POSTS, posts)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

}