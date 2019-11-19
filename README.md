# yande.re viewer
A simple yande.re viewer for Android

[yande.re official site](https://yande.re)

# Features
* viewer
* highlight unread posts
* save images
    - one or multi choice
    - you can change the save directory
* search posts from tags
    - you can use tag suggestions
* share a post
    - title and url
    - Twitter (must be turned on from settings)

# ScreenShots
view tweet

<blockquote class="twitter-tweet"><p lang="en" dir="ltr">screenshots of yandere viewer for readme <a href="https://t.co/BP6jzztSaG">pic.twitter.com/BP6jzztSaG</a></p>&mdash; yui (@sugtao4423) <a href="https://twitter.com/sugtao4423/status/1196789238311686146?ref_src=twsrc%5Etfw">November 19, 2019</a></blockquote>

# How to use
* Compile with AndroidStudio
* Add string to strings xml file

`app/src/main/res/values/strings.xml`
```
<string name="twitter_ck">YOUR_CONSUMER_KEY</string>
<string name="twitter_cs">YOUR_CONSUMER_SECRET</string>
```

If you don't use "Share on Twitter", you can use empty string
```
<string name="twitter_ck"></string>
<string name="twitter_cs"></string>
```

# Languages
* English (default) (like machine translate)
* Japanese (native)

