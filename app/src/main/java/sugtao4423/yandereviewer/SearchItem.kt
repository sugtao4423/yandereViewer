package sugtao4423.yandereviewer

data class SearchItem(
        val name: String,
        val kind: Int
) {
    companion object {
        const val TAG = 0
        const val HISTORY = 1
    }

    override fun toString(): String {
        return name
    }
}
