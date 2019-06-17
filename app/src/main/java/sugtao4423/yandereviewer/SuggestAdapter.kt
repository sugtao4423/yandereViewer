package sugtao4423.yandereviewer

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class SuggestAdapter(context: Context) : ArrayAdapter<SearchItem>(context, android.R.layout.simple_list_item_1) {

    private val mInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    data class ViewHolder(
            val icon: ImageView,
            val text: TextView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = mInflater.inflate(R.layout.item_dialog_icontext, parent, false)
            val icon = view.findViewById<ImageView>(R.id.dialog_image)
            val text = view.findViewById<TextView>(R.id.dialog_text)

            holder = ViewHolder(icon, text)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val item = getItem(position) ?: return view!!

        if (item.kind == SearchItem.TAG) {
            holder.icon.setImageResource(R.drawable.ic_menu_attachment)
        } else if (item.kind == SearchItem.HISTORY) {
            holder.icon.setImageResource(android.R.drawable.ic_menu_recent_history)
        }
        holder.text.text = item.name

        return view!!
    }

}