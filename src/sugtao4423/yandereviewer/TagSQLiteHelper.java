package sugtao4423.yandereviewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TagSQLiteHelper extends SQLiteOpenHelper{

	public static final String CREATE_DB_TABLE = "create table tags(id, name, count, type, ambiguous)";
	public static final String DROP_DB_TABLE = "drop table tags";

	public TagSQLiteHelper(Context context){
		super(context, "tags", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_DB_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}
}
