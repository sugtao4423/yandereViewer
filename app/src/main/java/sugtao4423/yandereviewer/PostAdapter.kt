package sugtao4423.yandereviewer

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.loopj.android.image.SmartImageView
import yandere4j.Post
import yandere4j.Yandere4j

class PostAdapter(private val mainActivity: MainActivity) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var readedId = PreferenceManager.getDefaultSharedPreferences(mainActivity.applicationContext).getLong(Keys.READEDID, -1L)
    private val data = ArrayList<Post>()
    private val inflater = mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val multiSelectedItems = HashMap<Post, Int>()
    private var cardViewBGColor = CardView(mainActivity).cardBackgroundColor.defaultColor

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.grid_item_layout, viewGroup, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.size <= position) {
            return
        }
        val item = data[position]

        holder.itemView.setOnClickListener(mainActivity.getOnCardClickListener(item))
        holder.itemView.setOnLongClickListener(mainActivity.getOnCardLongClickListener())

        holder.image.userAgent = Yandere4j.USER_AGENT
        holder.image.setImageUrl(item.preview.url, null, R.drawable.ic_action_refresh)
        holder.imageSize.text = "${item.file.width}x${item.file.height}"

        holder.imageSize.setBackgroundColor(
                if (readedId < item.id) {
                    Color.parseColor("#e1bee7")
                } else {
                    Color.parseColor("#ffffff")
                }
        )

        holder.itemView.setBackgroundColor(
                if (multiSelectedItems.keys.contains(item)) {
                    Color.parseColor("#B3E5FC")
                } else {
                    cardViewBGColor
                }
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun clear() {
        val size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun add(post: Post) {
        data.add(post)
        notifyItemInserted(data.size - 1)
    }

    fun addAll(posts: Array<Post>) {
        val pos = data.size
        posts.map {
            data.add(it)
        }
        notifyItemRangeInserted(pos, posts.size)
    }

    fun remove(post: Post) {
        val pos = data.indexOf(post)
        data.remove(post)
        notifyItemRemoved(pos)
    }

    fun isPostSelected(post: Post): Boolean {
        return multiSelectedItems.keys.contains(post)
    }

    fun setPostSelected(post: Post, isSelect: Boolean) {
        val pos = data.indexOf(post)
        if (isSelect) {
            multiSelectedItems[post] = pos
        } else {
            multiSelectedItems.remove(post)
        }
        notifyItemChanged(pos)
    }

    fun getSelectedPosts(): Array<Post> {
        return multiSelectedItems.keys.toTypedArray()
    }

    fun clearSelectedPosts() {
        val destroySelectedPostPos = multiSelectedItems.values.toTypedArray()
        multiSelectedItems.clear()
        destroySelectedPostPos.map {
            notifyItemChanged(it)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: SmartImageView = itemView.findViewById(R.id.postImage)
        val imageSize: TextView = itemView.findViewById(R.id.imageSize)
    }

}