package sugtao4423.yandereviewer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.commit {
            replace(android.R.id.content, PreferencesFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class PreferencesFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)

            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)

            val howView = findPreference<ListPreference>("how_view")!!
            howView.summary = getHowViewSummary(pref.getString(Keys.HOWVIEW, Keys.VAL_FULL)
                    ?: Keys.VAL_FULL)
            howView.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = getHowViewSummary(newValue as String)
                true
            }

            val twitter = findPreference<Preference>("twitter")!!
            val username = pref.getString(Keys.TWITTER_USERNAME, "")
            if (username == "") {
                twitter.title = getString(R.string.cooperate_with_twitter)
            } else {
                twitter.title = getString(R.string.cancel_collaboration)
                twitter.summary = username
            }
            twitter.setOnPreferenceClickListener {
                if (username == "") {
                    startActivity(Intent(requireContext(), TwitterOAuth::class.java))
                    requireActivity().finish()
                } else {
                    AlertDialog.Builder(requireContext()).apply {
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
                            Toast.makeText(requireContext().applicationContext, R.string.cancelled_collaboration, Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                        }
                        show()
                    }
                }
                true
            }

            val requestLimit = findPreference<Preference>("reqPostCount")!!
            requestLimit.summary = pref.getInt(Keys.REQUEST_POSTCOUNT, 50).toString()
            requestLimit.setOnPreferenceClickListener {
                val eLimit = EditText(requireContext())
                val dialogLayout = getEditTextDialogLayout(eLimit, true)
                eLimit.apply {
                    setText(pref.getInt(Keys.REQUEST_POSTCOUNT, 50).toString())
                    hint = "1 to 100"
                    inputType = InputType.TYPE_CLASS_NUMBER
                }
                AlertDialog.Builder(requireContext()).apply {
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

            val changeSaveDir = findPreference<Preference>("changeSaveDir")!!
            val externalStorageDir = Environment.getExternalStorageDirectory().absolutePath + "/"
            val defaultSaveDir = Environment.DIRECTORY_DOWNLOADS + "/"
            changeSaveDir.summary = externalStorageDir + pref.getString(Keys.SAVEDIR, defaultSaveDir)
            changeSaveDir.setOnPreferenceClickListener {
                val dirText = ChangeSaveDirEditText(requireContext())
                dirText.prefix = externalStorageDir
                val dialogLayout = getEditTextDialogLayout(dirText, false)
                val currentDir = pref.getString(Keys.SAVEDIR, defaultSaveDir)
                dirText.setText(currentDir)

                val changeSaveDirDialog = AlertDialog.Builder(requireContext()).run {
                    setTitle(getString(R.string.changeSaveDir))
                    setMessage(R.string.changeSaveDirDescription)
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

            val refreshAllTags = findPreference<Preference>("refreshAllTags")!!
            refreshAllTags.setOnPreferenceClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.refreshAllTags))
                    setMessage(getString(R.string.is_this_okay))
                    setNegativeButton("Cancel", null)
                    setPositiveButton("OK") { _, _ ->
                        DBUtils(requireContext().applicationContext).apply {
                            deleteAllTags()
                            close()
                        }
                        (requireContext().applicationContext as App).isRefreshTags = true
                        startActivity(Intent(requireContext(), SaveTagActivity::class.java))
                    }
                    show()
                }
                true
            }

            val clearHistory = findPreference<Preference>("clearHistory")!!
            clearHistory.setOnPreferenceClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.history_clear))
                    setMessage(getString(R.string.is_this_okay))
                    setNegativeButton("Cancel", null)
                    setPositiveButton("OK") { _, _ ->
                        (requireContext().applicationContext as App).clearedHistory = pref.edit().remove(Keys.SEARCH_HISTORY).commit()
                        Toast.makeText(requireContext().applicationContext, R.string.history_cleared, Toast.LENGTH_SHORT).show()
                    }
                    show()
                }
                true
            }

            val clearCache = findPreference<Preference>("clearCache")!!
            clearCache.summary = getString(R.string.cache, getCacheSize())
            clearCache.setOnPreferenceClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        Glide.get(requireContext()).clearDiskCache()
                    }
                    it.summary = getString(R.string.cache, getCacheSize())
                    Toast.makeText(requireContext().applicationContext, R.string.cache_cleared, Toast.LENGTH_SHORT).show()
                }
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
                return it.format(getDirSize(requireContext().cacheDir).toDouble() / 1024 / 1024) + "MB"
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
            val editContainer = FrameLayout(requireContext())
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

    class ChangeSaveDirEditText(context: Context) : androidx.appcompat.widget.AppCompatEditText(context) {

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