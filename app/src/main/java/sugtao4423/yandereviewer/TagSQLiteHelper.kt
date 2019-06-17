package sugtao4423.yandereviewer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TagSQLiteHelper(context: Context) : SQLiteOpenHelper(context, "tags", null, 1) {

    companion object {
        const val CREATE_DB_TABLE = "create table tags(id, name, count, type, ambiguous)"
        const val DROP_DB_TABLE = "drop table tags"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_DB_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}