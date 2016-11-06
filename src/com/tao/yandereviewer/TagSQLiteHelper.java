package com.tao.yandereviewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TagSQLiteHelper extends SQLiteOpenHelper{

	public TagSQLiteHelper(Context context){
		super(context, "tags", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("create table tags(id, name, count, type, ambiguous)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}
}
