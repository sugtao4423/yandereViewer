package sugtao4423.yandereviewer

import android.content.ContentValues
import android.content.Context
import yandere4j.Tag

class DBUtils(context: Context) {

    private val db = TagSQLiteHelper(context.applicationContext).writableDatabase

    fun writeTags(tags: Array<Tag>) {
        val contentValues = arrayListOf<ContentValues>()
        tags.map {
            val values = ContentValues().apply {
                put("id", it.id)
                put("name", it.name)
                put("count", it.count)
                put("type", it.type)
                put("ambiguous", if (it.ambiguous) 1 else 0)
            }
            contentValues.add(values)
        }
        db.beginTransaction()
        contentValues.map {
            db.insert("tags", null, it)
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun loadTags(): Array<Tag> {
        val c = db.rawQuery("select * from tags", null)
        var mov = c.moveToFirst()
        val result = arrayListOf<Tag>()
        while (mov) {
            c.apply {
                val id = getInt(0)
                val name = getString(1)
                val count = getInt(2)
                val type = getInt(3)
                val ambiguous = getInt(4) == 1
                result.add(Tag(id, name, count, type, ambiguous))
                mov = moveToNext()
            }
        }
        c.close()
        return result.toTypedArray()
    }

    fun loadTagNamesAsArrayList(): ArrayList<String> {
        val result = arrayListOf<String>()
        val c = db.rawQuery("select name from tags", null)
        var mov = c.moveToFirst()
        while (mov) {
            result.add(c.getString(0))
            mov = c.moveToNext()
        }
        c.close()
        return result
    }

    fun deleteAllTags() {
        db.execSQL(TagSQLiteHelper.DROP_DB_TABLE)
        db.execSQL(TagSQLiteHelper.CREATE_DB_TABLE)
    }

    fun close() {
        db.close()
    }

}