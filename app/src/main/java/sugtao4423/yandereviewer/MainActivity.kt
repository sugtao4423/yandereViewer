package sugtao4423.yandereviewer

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sugtao4423.icondialog.IconDialog
import sugtao4423.icondialog.IconItem
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder
import yandere4j.Post
import yandere4j.Yandere4j

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        const val INTENT_EXTRA_SEARCHQUERY = "searchQuery"
        const val SAMPLE = 0
        const val FULL = 1
        const val ASK = 2
    }

    private lateinit var app: App

    private var multiSelectMode: ActionMode? = null
    private lateinit var scrollListener: EndlessScrollListener
    private lateinit var adapter: PostAdapter
    private val yandere = Yandere4j()
    private var yanderePage = 1
    private var searchQuery: String? = null

    private lateinit var pref: SharedPreferences
    private var howView = ASK

    private var twitter: Twitter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        adapter = PostAdapter(this)
        grid.adapter = adapter

        scrollListener = getScrollListener(grid.gridLayoutManager)
        grid.addOnScrollListener(scrollListener)

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        swipeRefresh.setOnRefreshListener(this)

        app = applicationContext as App
        pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        loadSettings()

        searchQuery = intent.getStringExtra(INTENT_EXTRA_SEARCHQUERY)
        if (searchQuery != null) {
            supportActionBar?.title = searchQuery
            supportActionBar?.setIcon(android.R.drawable.ic_menu_search)
        }

        if (!pref.getBoolean(Keys.TAGSAVED, false)) {
            val i = Intent(this, SaveTagActivity::class.java)
            i.putExtra(SaveTagActivity.INTENT_EXTRA_STARTMAIN, true)
            startActivity(i)
            finish()
            return
        } else {
            loadPosts(false)
        }
    }

    private fun loadPosts(isRefresh: Boolean) {
        if (isRefresh) {
            adapter.clear()
            yanderePage = 1
        }
        CoroutineScope(Dispatchers.Main).launch {
            swipeRefresh.isRefreshing = true
            val result = withContext(Dispatchers.IO) {
                try {
                    if (searchQuery == null) {
                        yandere.getPosts(yanderePage)
                    } else {
                        yandere.searchPosts(searchQuery!!, yanderePage)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            swipeRefresh.isRefreshing = false
            if (result == null) {
                Toast.makeText(this@MainActivity, R.string.get_error, Toast.LENGTH_LONG).show()
                return@launch
            }
            adapter.addAll(result)
            if (result.size < yandere.requestPostCount) {
                scrollListener.stopOnLoadMore = true
            }
            if (yanderePage == 1 && searchQuery == null) {
                pref.edit().putLong(Keys.READEDID, result[0].id).commit()
            }
            yanderePage++
        }
    }

    override fun onRefresh() {
        scrollListener.resetState()
        loadPosts(true)
    }

    private fun getScrollListener(glm: GridLayoutManager): EndlessScrollListener {
        return object : EndlessScrollListener(glm) {
            override fun onLoadMore(currentPage: Int) {
                loadPosts(false)
            }
        }
    }

    fun getOnCardClickListener(post: Post): View.OnClickListener {
        return View.OnClickListener {
            if (multiSelectMode != null) {
                adapter.setPostSelected(post, !adapter.isPostSelected(post))
                val selectedCount = adapter.getSelectedPosts().size
                multiSelectMode!!.title = "$selectedCount selected"
                if (selectedCount == 0) {
                    multiSelectMode!!.finish()
                }
                return@OnClickListener
            }

            val openText = when (howView) {
                ASK -> getString(R.string.open)
                SAMPLE -> getString(R.string.view_sample_size) + " (" + App.getFileMB(post.sample.size) + ")"
                FULL -> getString(R.string.view_full_size) + " (" + App.getFileMB(post.file.size) + ")"
                else -> ""
            }

            val items = arrayListOf<IconItem>().run {
                add(IconItem(openText, android.R.drawable.ic_menu_gallery))
                add(IconItem(getString(R.string.open_full_size_on_browser), android.R.drawable.ic_menu_set_as))
                add(IconItem(getString(R.string.save_full_size), android.R.drawable.ic_menu_save))
                add(IconItem(getString(R.string.share), android.R.drawable.ic_menu_share))
                if (twitter == null) {
                    add(IconItem(getString(R.string.detail), android.R.drawable.ic_menu_info_details))
                } else {
                    add(IconItem(getString(R.string.share_on_twitter), R.drawable.twitter_social_icon_blue))
                    add(IconItem(getString(R.string.detail), android.R.drawable.ic_menu_info_details))
                }
                toTypedArray()
            }
            val dialog = IconDialog(this)
            dialog.setItems(items, DialogInterface.OnClickListener IconDialogOnClickListener@{ _, which ->
                fun openDetail(post: Post) {
                    val i = Intent(this, PostDetail::class.java)
                    i.putExtra(PostDetail.INTENT_EXTRA_POSTDATA, post)
                    startActivity(i)
                }
                when (which) {
                    0 -> {
                        val i = Intent(this, ShowImage::class.java)
                        when (howView) {
                            ASK -> {
                                val sampleSize = " (" + App.getFileMB(post.sample.size) + ")"
                                val fullSize = " (" + App.getFileMB(post.file.size) + ")"
                                AlertDialog.Builder(this).apply {
                                    setItems(arrayOf(
                                            getString(R.string.open_sample_size) + sampleSize,
                                            getString(R.string.open_full_size) + fullSize
                                    )) { _, which ->
                                        if (which == 0) {
                                            i.putExtra(ShowImage.INTENT_EXTRA_URL, post.sample.url)
                                            i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.sample.size)
                                        } else if (which == 1) {
                                            i.putExtra(ShowImage.INTENT_EXTRA_URL, post.file.url)
                                            i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.file.size)
                                        }
                                        startActivity(i)
                                    }
                                    show()
                                }
                            }
                            else -> {
                                if (howView == SAMPLE) {
                                    i.putExtra(ShowImage.INTENT_EXTRA_URL, post.sample.url)
                                    i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.sample.size)
                                } else if (howView == FULL) {
                                    i.putExtra(ShowImage.INTENT_EXTRA_URL, post.file.url)
                                    i.putExtra(ShowImage.INTENT_EXTRA_FILESIZE, post.file.size)
                                }
                                startActivity(i)
                            }
                        }
                    }
                    1 -> ChromeIntent(this, post.file.url)
                    2 -> app.saveImage(this, post)
                    3 -> {
                        val i = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, yandere.getShareText(post))
                        }
                        startActivity(i)
                    }
                    4 -> {
                        if (twitter == null) {
                            openDetail(post)
                            return@IconDialogOnClickListener
                        }
                        val i = Intent(this, TweetActivity::class.java)
                        i.putExtra(TweetActivity.INTENT_EXTRA_POST, post)
                        startActivity(i)
                    }
                    5 -> openDetail(post)
                }
            })
            dialog.show()
        }
    }

    fun getOnCardLongClickListener(): View.OnLongClickListener {
        return View.OnLongClickListener {
            startSupportActionMode(object : ActionMode.Callback {

                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    multiSelectMode = mode
                    menu?.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Save All")?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    it.callOnClick()
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    if (item?.itemId == Menu.FIRST) {
                        app.saveImages(this@MainActivity, adapter.getSelectedPosts())
                        mode?.finish()
                    }
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    multiSelectMode = null
                    adapter.clearSelectedPosts()
                }
            })
            true
        }
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
        if (searchQuery == null && (app.clearedHistory || app.isRefreshTags)) {
            invalidateOptionsMenu()
            app.clearedHistory = false
            app.isRefreshTags = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
    }

    private fun loadSettings() {
        howView = when (pref.getString(Keys.HOWVIEW, Keys.VAL_FULL)) {
            Keys.VAL_SAMPLE -> SAMPLE
            Keys.VAL_FULL -> FULL
            else -> ASK
        }

        if (pref.getString(Keys.TWITTER_USERNAME, "") != "") {
            val conf = ConfigurationBuilder().run {
                setOAuthConsumerKey(getString(R.string.twitter_ck))
                setOAuthConsumerSecret(getString(R.string.twitter_cs))
                build()
            }
            val at = AccessToken(pref.getString(Keys.TWITTER_AT, null), pref.getString(Keys.TWITTER_ATS, null))
            twitter = TwitterFactory(conf).getInstance(at)
        } else {
            twitter = null
        }
        yandere.requestPostCount = pref.getInt(Keys.REQUEST_POSTCOUNT, 50)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (searchQuery == null) {
            MenuInflater(this).inflate(R.menu.menu_both, menu)
            val mactv = (menu!!.findItem(R.id.search_view).actionView).findViewById(R.id.cactv) as MultiAutoCompleteTextView
            mactv.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    val dbUtils = DBUtils(applicationContext)
                    val tagNames = dbUtils.loadTagNamesAsArrayList()
                    dbUtils.close()
                    tagNames
                }
                prepareSuggest(result, mactv)
                mactv.isEnabled = true
            }
        } else {
            MenuInflater(this).inflate(R.menu.menu_settings, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun prepareSuggest(tags: ArrayList<String>, mactv: MultiAutoCompleteTextView) {
        val suggestAdapter = SuggestAdapter(this)

        val searchHistory = (pref.getString(Keys.SEARCH_HISTORY, "") ?: "").split(",")
        searchHistory.map {
            suggestAdapter.add(SearchItem(it, SearchItem.HISTORY))
        }

        tags.map {
            suggestAdapter.add(SearchItem(it, SearchItem.TAG))
        }

        mactv.setAdapter(suggestAdapter)
        mactv.hint = "Search post from tag"
        mactv.setTokenizer(SpaceTokenizer())
        mactv.setOnEditorActionListener { _, actionId, event ->
            if ((event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                val query = mactv.text.toString().replace(Regex("\\s+$"), "")

                val history = arrayListOf<String>()
                (pref.getString(Keys.SEARCH_HISTORY, "") ?: "").split(",").map {
                    if (it.isNotEmpty()) {
                        history.add(it)
                    }
                }
                if (history.indexOf(query) == -1 && tags.indexOf(query) == -1) {
                    history.add(query)
                    val result = history.joinToString()
                    pref.edit().putString(Keys.SEARCH_HISTORY, result).commit()
                    suggestAdapter.add(SearchItem(query, SearchItem.HISTORY))
                }

                val i = Intent(applicationContext, MainActivity::class.java)
                i.putExtra(INTENT_EXTRA_SEARCHQUERY, query)
                startActivity(i)
            }
            false
        }
        mactv.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View?) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(v, 0)
            }

            override fun onViewDetachedFromWindow(v: View?) {
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.order == Menu.FIRST + 1) {
            startActivity(Intent(this, Settings::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

}
