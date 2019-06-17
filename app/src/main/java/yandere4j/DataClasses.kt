package yandere4j

import java.io.Serializable
import java.util.*

data class Preview(
    val url: String,
    val width: Int,
    val height: Int
) : Serializable

data class Sample(
    val url: String,
    val size: Int,
    val width: Int,
    val height: Int
) : Serializable

data class Jpeg(
    val url: String,
    val size: Int,
    val width: Int,
    val height: Int
) : Serializable

data class File(
    val url: String,
    val ext: String,
    val size: Int,
    val width: Int,
    val height: Int
) : Serializable

data class Post(
    val file: File,
    val preview: Preview,
    val sample: Sample,

    val id: Long,
    val parentId: Long,
    val change: Long,

    val tags: Array<String>,

    val creatorId: String,
    val approverId: String,
    val author: String,
    val source: String,
    val md5: String,
    val rating: String,
    val status: String,

    val createdAt: Date,
    val updatedAt: Date,

    val isShownInIndex: Boolean,
    val isRatingLocked: Boolean,
    val hasChildren: Boolean,
    val isPending: Boolean,
    val isHeld: Boolean,
    val isNoteLocked: Boolean,

    val score: Int,
    val lastNotedAt: Int,
    val lastCommentedAt: Int
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        if (file != other.file) return false
        if (preview != other.preview) return false
        if (sample != other.sample) return false
        if (id != other.id) return false
        if (parentId != other.parentId) return false
        if (change != other.change) return false
        if (!tags.contentEquals(other.tags)) return false
        if (creatorId != other.creatorId) return false
        if (approverId != other.approverId) return false
        if (author != other.author) return false
        if (source != other.source) return false
        if (md5 != other.md5) return false
        if (rating != other.rating) return false
        if (status != other.status) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (isShownInIndex != other.isShownInIndex) return false
        if (isRatingLocked != other.isRatingLocked) return false
        if (hasChildren != other.hasChildren) return false
        if (isPending != other.isPending) return false
        if (isHeld != other.isHeld) return false
        if (isNoteLocked != other.isNoteLocked) return false
        if (score != other.score) return false
        if (lastNotedAt != other.lastNotedAt) return false
        if (lastCommentedAt != other.lastCommentedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + preview.hashCode()
        result = 31 * result + sample.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + parentId.hashCode()
        result = 31 * result + change.hashCode()
        result = 31 * result + tags.contentHashCode()
        result = 31 * result + creatorId.hashCode()
        result = 31 * result + approverId.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + md5.hashCode()
        result = 31 * result + rating.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + isShownInIndex.hashCode()
        result = 31 * result + isRatingLocked.hashCode()
        result = 31 * result + hasChildren.hashCode()
        result = 31 * result + isPending.hashCode()
        result = 31 * result + isHeld.hashCode()
        result = 31 * result + isNoteLocked.hashCode()
        result = 31 * result + score
        result = 31 * result + lastNotedAt
        result = 31 * result + lastCommentedAt
        return result
    }
}

data class Tag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    val ambiguous: Boolean
) : Comparable<Tag> {
    override fun compareTo(other: Tag): Int {
        return this.id - other.id
    }
}