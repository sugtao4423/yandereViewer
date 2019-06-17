package sugtao4423.yandereviewer

/*
 * Thank you!!
 * https://qiita.com/le_skamba/items/cca74696095cbbb65cc3
 */

import android.net.Uri
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView

/**
 * Htmlテキストのリンククリックアクションをオーバーライドするためのクラス。<br>
 * <p>
 * original source is android.text.method.LinkMovementMethod.java
 *
 * @author S.Kamba
 */

class MutableLinkMovementMethod : LinkMovementMethod() {

    /**
     * Urlのリンクをタップした時のイベントを受け取るリスナー
     */
    interface OnUrlClickListener {
        fun onUrlClick(widget: TextView, uri: Uri)
    }

    /**
     * Urlクリックリスナー
     */
    private var listener: OnUrlClickListener? = null

    /*
     * Urlクリックリスナーを登録
     */
    fun setOnUrlClickListener(l: OnUrlClickListener) {
        listener = l
    }

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        if (widget == null || buffer == null || event == null) {
            return super.onTouchEvent(widget, buffer, event)
        }

        // LinkMovementMethod#onTouchEventそのまんま

        val action = event.action

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = buffer.getSpans(off, off, ClickableSpan::class.java)

            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    // リスナーがあればそちらを呼び出し
                    if (link[0] is URLSpan && listener != null) {
                        val uri = Uri.parse((link[0] as URLSpan).url)
                        listener!!.onUrlClick(widget, uri)
                    } else {
                        link[0].onClick(widget)
                    }
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]))
                }

                return true
            } else {
                Selection.removeSelection(buffer)
            }
        }

        return super.onTouchEvent(widget, buffer, event)
    }

}