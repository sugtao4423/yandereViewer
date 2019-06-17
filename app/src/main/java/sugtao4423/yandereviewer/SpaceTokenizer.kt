package sugtao4423.yandereviewer

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.widget.MultiAutoCompleteTextView

class SpaceTokenizer : MultiAutoCompleteTextView.Tokenizer {

    override fun findTokenStart(text: CharSequence?, cursor: Int): Int {
        if (text == null) {
            return cursor
        }

        var i = cursor
        while (i > 0 && text[i - 1] != ' ') {
            i--
        }
        while (i < cursor && text[i] == ' ') {
            i++
        }
        return i
    }

    override fun findTokenEnd(text: CharSequence?, cursor: Int): Int {
        if (text == null) {
            return cursor
        }

        var i = cursor
        val len = text.length
        while (i < len) {
            if (text[i] == ' ') {
                return i
            } else {
                i++
            }
        }
        return len
    }

    override fun terminateToken(text: CharSequence?): CharSequence {
        if (text == null) {
            return ""
        }

        var i = text.length
        while (i > 0 && text[i - 1] == ' ') {
            i--
        }
        return if (i > 0 && text[i - 1] == ' ') {
            text
        } else {
            if (text is Spanned) {
                val sp = SpannableString("$text ")
                TextUtils.copySpansFrom(text, 0, text.length, Object::class.java, sp, 0)
                sp
            } else {
                "$text "
            }
        }
    }

}