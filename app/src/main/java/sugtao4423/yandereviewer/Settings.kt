package sugtao4423.yandereviewer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.InputType
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import java.io.File
import java.text.DecimalFormat

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, PreferencesFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            if (activity == null) {
                return
            }
            val activity = activity!!
            setPreferencesFromResource(R.xml.settings, rootKey)

            val pref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

            val howView = findPreference("how_view") as ListPreference
            howView.summary = getHowViewSummary(pref.getString(Keys.HOWVIEW, Keys.VAL_FULL)
                    ?: Keys.VAL_FULL)
            howView.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = getHowViewSummary(newValue as String)
                true
            }

            val twitter = findPreference("twitter")
            val username = pref.getString(Keys.TWITTER_USERNAME, "")
            if (username == "") {
                twitter.title = getString(R.string.cooperate_with_twitter)
            } else {
                twitter.title = getString(R.string.cancel_collaboration)
                twitter.summary = username
            }
            twitter.setOnPreferenceClickListener {
                if (username == "") {
                    startActivity(Intent(activity, TwitterOAuth::class.java))
                    activity.finish()
                } else {
                    AlertDialog.Builder(activity).apply {
                        it.title = getString(R.string.cancel_collaboration)
                        setMessage(getString(R.string.is_this_okay))
                        setNegativeButton("Cancel", null)
                        setPositiveButton("OK") { _, _ ->
                            pref.edit().apply {
                                putString(Keys.TWITTER_USERNAME, "")
                                putString(Keys.TWITTER_AT, "")
                                putString(Keys.TWITTER_ATS, "")
                                commit()
                            }
                            Toast.makeText(activity.applicationContext, R.string.cancelled_collaboration, Toast.LENGTH_SHORT).show()
                            activity.finish()
                        }
                        show()
                    }
                }
                true
            }

            val requestLimit = findPreference("reqPostCount")
            requestLimit.summary = pref.getInt(Keys.REQUEST_POSTCOUNT, 50).toString()
            requestLimit.setOnPreferenceClickListener {
                val eLimit = EditText(activity)
                val dialogLayout = getEditTextDialogLayout(eLimit, true)
                eLimit.apply {
                    setText(pref.getInt(Keys.REQUEST_POSTCOUNT, 50).toString())
                    hint = "1 to 100"
                    inputType = InputType.TYPE_CLASS_NUMBER
                }
                AlertDialog.Builder(activity).apply {
                    setView(dialogLayout)
                    setNegativeButton("Cancel", null)
                    setPositiveButton("OK") { _, _ ->
                        pref.edit().putInt(Keys.REQUEST_POSTCOUNT, eLimit.text.toString().toInt()).commit()
                        it.summary = eLimit.text.toString()
                    }
                    show()
                }
                true
            }

            val changeSaveDir = findPreference("changeSaveDir")
            val externalStorageDir = Environment.getExternalStorageDirectory().absolutePath + "/"
            val defaultSaveDir = Environment.DIRECTORY_DOWNLOADS + "/"
            changeSaveDir.summary = externalStorageDir + pref.getString(Keys.SAVEDIR, defaultSaveDir)
            changeSaveDir.setOnPreferenceClickListener {
                val dirText = ChangeSaveDirEditText(activity)
                dirText.prefix = externalStorageDir
                val dialogLayout = getEditTextDialogLayout(dirText, false)
                val currentDir = pref.getString(Keys.SAVEDIR, defaultSaveDir)
                dirText.setText(currentDir)

                val changeSaveDirDialog = AlertDialog.Builder(activity).run {
                    setTitle(getString(R.string.changeSaveDir))
                    setView(dialogLayout)
                    setNegativeButton("Cancel", null)
                    setNeutralButton("Default", null)
                    setPositiveButton("OK") { _, _ ->
                        var current = dirText.text.toString()
                        if (!current.endsWith("/")) {
                            current += "/"
                        }
                        val fDir = File(current)
                        if (!fDir.exists()) {
                            fDir.mkdirs()
                        }
                        if (pref.edit().putString(Keys.SAVEDIR, current).commit()) {
                            it.summary = externalStorageDir + current
                        }
                    }
                    show()
                }
                changeSaveDirDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    dirText.setText(defaultSaveDir)
                }
                true
            }

            val refreshAllTags = findPreference("refreshAllTags")
            refreshAllTags.setOnPreferenceClickListener {
                AlertDialog.Builder(activity).apply {
                    setTitle(getString(R.string.refreshAllTags))
                    setMessage(getString(R.string.is_this_okay))
                    setNegativeButton("Cancel", null)
                    setPositiveButton("OK") { _, _ ->
                        DBUtils(activity.applicationContext).apply {
                            deleteAllTags()
                            close()
                        }
                        (activity.applicationContext as App).isRefreshTags = true
                        startActivity(Intent(activity, SaveTagActivity::class.java))
                    }
                    show()
                }
                true
            }

            val clearHistory = findPreference("clearHistory")
            clearHistory.setOnPreferenceClickListener {
                AlertDialog.Builder(activity).apply {
                    setTitle(getString(R.string.history_clear))
                    setMessage(getString(R.string.is_this_okay))
                    setNegativeButton("Cancel", null)
                    setPositiveButton("OK") { _, _ ->
                        (activity.applicationContext as App).clearedHistory = pref.edit().remove(Keys.SEARCH_HISTORY).commit()
                        Toast.makeText(activity, R.string.history_cleared, Toast.LENGTH_SHORT).show()
                    }
                    show()
                }
                true
            }

            val clearCache = findPreference("clearCache")
            clearCache.summary = getString(R.string.cache, getCacheSize())
            clearCache.setOnPreferenceClickListener {
                object : AsyncTask<Void, Void, Void?>() {
                    override fun doInBackground(vararg params: Void?): Void? {
                        Glide.get(context!!).clearDiskCache()
                        return null
                    }

                    override fun onPostExecute(result: Void?) {
                        it.summary = getString(R.string.cache, getCacheSize())
                        Toast.makeText(activity.applicationContext, R.string.cache_cleared, Toast.LENGTH_SHORT).show()
                    }
                }.execute()
                true
            }
        }

        private fun getCacheSize(): String {
            fun getDirSize(dir: File): Long {
                var size = 0L
                dir.listFiles().map {
                    when {
                        it == null -> return@map
                        it.isDirectory -> size += getDirSize(it)
                        it.isFile -> size += it.length()
                    }
                }
                return size
            }
            DecimalFormat("#.#").let {
                it.minimumFractionDigits = 2
                it.maximumFractionDigits = 2
                return it.format(getDirSize(context!!.cacheDir).toDouble() / 1024 / 1024) + "MB"
            }
        }

        private fun getHowViewSummary(str: String): String {
            return when (str) {
                Keys.VAL_SAMPLE -> getString(R.string.sample_size)
                Keys.VAL_FULL -> getString(R.string.full_size)
                Keys.VAL_ASK -> getString(R.string.ask)
                else -> ""
            }
        }

        private fun getEditTextDialogLayout(editText: EditText, isTopMargin: Boolean): FrameLayout {
            val editContainer = FrameLayout(activity!!)
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val margin = ((24 * resources.displayMetrics.density) + 0.5).toInt()
            params.apply {
                if (isTopMargin) {
                    topMargin = margin
                }
                leftMargin = margin
                rightMargin = margin
            }
            editText.layoutParams = params
            editContainer.addView(editText)
            return editContainer
        }

    }

    class ChangeSaveDirEditText(context: Context) : EditText(context) {

        var prefix = ""
        private val prefixRect = Rect()

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            paint.getTextBounds(prefix, 0, prefix.length, prefixRect)
            prefixRect.right += paint.measureText("").toInt()
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            canvas?.drawText(prefix, super.getCompoundPaddingLeft().toFloat(), baseline.toFloat(), paint)
        }

        override fun getCompoundPaddingLeft(): Int {
            return super.getCompoundPaddingLeft() + prefixRect.width()
        }

    }

}