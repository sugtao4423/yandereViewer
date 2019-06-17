package sugtao4423.yandereviewer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import yandere4j.Post
import yandere4j.Yandere4j
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
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
                setSmallIcon(R.drawable.ic_launcher)
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

    private fun saveImages(saveList: Array<Post>) {
        object : AsyncTask<Unit, Unit, Unit>() {
            private lateinit var saveDir: String

            override fun onPreExecute() {
                val defaultPath = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DOWNLOADS + "/"
                saveDir = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(Keys.SAVEDIR, defaultPath)
                        ?: defaultPath
            }

            override fun doInBackground(vararg params: Unit?) {
                saveList.mapIndexed { i, it ->
                    val time = System.currentTimeMillis()
                    val notifiId = i
                    val builder = NotificationCompat.Builder(applicationContext, "default").apply {
                        setContentTitle("Saving... " + (i + 1) + "/" + saveList.size)
                        setContentText(Yandere4j().getFileName(it))
                        setSmallIcon(android.R.drawable.stat_sys_download)
                        setProgress(100, 0, false)
                        setOngoing(true)
                        setWhen(time)
                    }
                    notificationManager.notify(notifiId, builder.build())

                    val path = saveDir + Yandere4j().getFileName(it)
                    try {
                        doDownload(it, builder, notifiId, path)
                    } catch (e: IOException) {
                        onIOErrorNotification(it, builder, notifiId, path, i, saveList.size)
                        return@mapIndexed
                    }
                    onSuccessNotification(builder, notifiId, path, i, saveList.size)
                }
            }

            override fun onPostExecute(result: Unit?) {
                stopSelf()
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @Throws(IOException::class)
    fun doDownload(post: Post, builder: NotificationCompat.Builder, notifiId: Int, savePath: String) {
        val conn = URL(post.file.url).openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", Yandere4j.USER_AGENT)
        conn.connect()
        val inputStream = conn.inputStream
        val fos = FileOutputStream(savePath)
        val buffer = ByteArray(1024)
        var len = inputStream.read(buffer)
        var percentage = 0
        var i = 1
        while (len > 0) {
            fos.write(buffer, 0, len)
            val currentPer = Math.round(++i * 1024.toFloat() / post.file.size * 100)
            if (percentage != currentPer) {
                builder.setProgress(100, currentPer, false)
                notificationManager.notify(notifiId, builder.build())
                percentage = currentPer
            }
            len = inputStream.read(buffer)
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
        }
        builder.setProgress(0, 0, false)
        notificationManager.notify(notifiId, builder.build())
        fos.close()
        inputStream.close()
        conn.disconnect()
    }

    fun onIOErrorNotification(post: Post, builder: NotificationCompat.Builder, notifiId: Int, savePath: String, currentPos: Int, allPostSize: Int) {
        val f = File(savePath)
        if (f.exists()) {
            f.delete()
        }
        val url = "https://yande.re/post/show/${post.id}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val pendingIntent = PendingIntent.getActivity(applicationContext, notifiId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.apply {
            setContentTitle(getString(R.string.save_failed) + " " + (currentPos + 1) + "/" + allPostSize)
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setOngoing(false)
            setContentIntent(pendingIntent)
            setAutoCancel(false)
        }
        notificationManager.notify(notifiId, builder.build())
    }

    fun onSuccessNotification(builder: NotificationCompat.Builder, notifiId: Int, savePath: String, currentPos: Int, allPostSize: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
            }
        }
        val picIntent = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(File(savePath)), "image/*")
        val contentIntent = PendingIntent.getActivity(applicationContext, notifiId, picIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.apply {
            setContentTitle(getString(R.string.save_success) + " " + (currentPos + 1) + "/" + allPostSize)
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setOngoing(false)
            setContentIntent(contentIntent)
            setAutoCancel(true)
        }
        notificationManager.notify(notifiId, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}