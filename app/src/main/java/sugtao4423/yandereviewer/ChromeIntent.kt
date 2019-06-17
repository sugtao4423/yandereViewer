package sugtao4423.yandereviewer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.support.customtabs.CustomTabsIntent

class ChromeIntent(context: Context, uri: Uri) {

    init {
        CustomTabsIntent.Builder().apply {
            setShowTitle(true)
            enableUrlBarHiding()
            addDefaultShareMenuItem()
            setToolbarColor(Color.parseColor(context.getString(R.color.primary)))
            build().launchUrl(context, uri)
        }
    }

    constructor(context: Context, url: String) : this(context, Uri.parse(url))

}