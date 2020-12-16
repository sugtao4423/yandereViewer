package sugtao4423.yandereviewer

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat

class ChromeIntent(context: Context, uri: Uri) {

    init {
        CustomTabsIntent.Builder().apply {
            setShowTitle(true)
            setUrlBarHidingEnabled(true)
            setShareState(CustomTabsIntent.SHARE_STATE_ON)
            val colorScheme = CustomTabColorSchemeParams.Builder().let {
                it.setToolbarColor(ContextCompat.getColor(context, R.color.primary))
                it.build()
            }
            setDefaultColorSchemeParams(colorScheme)
            build().launchUrl(context, uri)
        }
    }

    constructor(context: Context, url: String) : this(context, Uri.parse(url))

}