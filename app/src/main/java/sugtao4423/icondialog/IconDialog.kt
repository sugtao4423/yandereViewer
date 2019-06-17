package sugtao4423.icondialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import sugtao4423.yandereviewer.R

class IconDialog(private val context: Context) {

    private val builder = AlertDialog.Builder(context)

    fun setTitle(title: String): AlertDialog.Builder {
        return builder.setTitle(title)
    }

    fun setItems(items: Array<IconItem>, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
        val adapter = IconDialogAdapter(context, items)
        return builder.setAdapter(adapter, listener)
    }

    fun show(): AlertDialog {
        return builder.show()
    }

}

class IconDialogAdapter(context: Context, items: Array<IconItem>) :
        ArrayAdapter<IconItem>(context, android.R.layout.select_dialog_item, android.R.id.text1, items) {

    private val mInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    data class ViewHolder(
            val image: ImageView,
            val text: TextView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = mInflater.inflate(R.layout.item_dialog_icontext, parent, false)
            val image = view.findViewById<ImageView>(R.id.dialog_image)
            val text = view.findViewById<TextView>(R.id.dialog_text)

            holder = ViewHolder(image, text)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val item = getItem(position) ?: return view!!

        holder.image.setImageResource(item.resource)
        holder.text.text = item.title

        return view!!
    }
}