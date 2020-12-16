package sugtao4423.yandereviewer

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import yandere4j.Post
import java.io.File
import java.net.URLDecoder
import java.util.*

class DownloadService : Service() {

    companion object {
        const val INTENT_KEY_POSTS = "posts"
    }

    private lateinit var notificationManager: NotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }

        val objects = intent.getSerializableExtra(INTENT_KEY_POSTS) as Array<*>
        val posts = Arrays.copyOf(objects, objects.size, Array<Post>::class.java)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifiId = 114514
            val appIntent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val pendingIntent = PendingIntent.getActivity(applicationContext, notifiId, appIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val channelId = "default"
            val title = getString(R.string.app_name) + " download"
            val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(applicationContext, channelId).run {
                setContentTitle(title)
                setSmallIcon(android.R.drawable.stat_sys_download)
                setAutoCancel(true)
                setContentIntent(pendingIntent)
                setWhen(System.currentTimeMillis())
                build()
            }
            startForeground(notifiId, notification)
        }
        saveImages(posts)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun saveImages(posts: Array<Post>) {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val saveCanonicalDir = let {
            val defaultCanonicalDir = Environment.DIRECTORY_DOWNLOADS + "/"
            pref.getString(Keys.SAVEDIR, defaultCanonicalDir) ?: defaultCanonicalDir
        }
        val firstDirectory = saveCanonicalDir.split("/")[0]
        val saveFileAbsPath = saveCanonicalDir.replace(firstDirectory, "/") + "/"

        val dlManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var currentPos = 0
        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (currentPos == posts.size) {
                    unregisterReceiver(this)
                    stopSelf()
                    return
                }
                val post = posts[currentPos++]
                val fileName = File(URLDecoder.decode(post.file.url, "UTF-8")).name
                val dlRequest = DownloadManager.Request(Uri.parse(post.file.url)).apply {
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(firstDirectory, saveFileAbsPath + fileName)
                }
                dlManager.enqueue(dlRequest)
            }
        }

        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        downloadReceiver.onReceive(null, null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}