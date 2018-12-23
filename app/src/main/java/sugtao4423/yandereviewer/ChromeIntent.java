package sugtao4423.yandereviewer;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

public class ChromeIntent{

	public ChromeIntent(Context context, Uri uri){
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		builder.setShowTitle(true);
		builder.enableUrlBarHiding();
		builder.addDefaultShareMenuItem();
		builder.setToolbarColor(Color.parseColor(context.getString(R.color.primary)));
		builder.build().launchUrl(context, uri);
	}

	public ChromeIntent(Context context, String url){
		this(context, Uri.parse(url));
	}

}
