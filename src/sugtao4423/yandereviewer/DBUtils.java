package sugtao4423.yandereviewer;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import yandere4j.data.Tag;

public class DBUtils{

	private SQLiteDatabase db;

	public DBUtils(SQLiteDatabase db){
		this.db = db;
	}

	public void writeTags(Tag[] tags){
		ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
		for(Tag t : tags){
			ContentValues vals = new ContentValues();
			vals.put("id", t.getId());
			vals.put("name", t.getName());
			vals.put("count", t.getCount());
			vals.put("type", t.getType());
			vals.put("ambiguous", t.getAmbiguous() ? 1 : 0);
			contentValues.add(vals);
		}
		db.beginTransaction();
		for(ContentValues v : contentValues)
			db.insert("tags", null, v);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public Tag[] loadTags(){
		Cursor c = db.rawQuery("select * from tags", null);
		boolean mov = c.moveToFirst();
		Tag[] result = new Tag[c.getCount()];
		for(int i = 0; mov; i++){
			result[i] = new Tag(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4) == 1 ? true : false);
			mov = c.moveToNext();
		}
		c.close();
		return result;
	}

	public ArrayList<String> loadTagNamesAsArrayList(){
		ArrayList<String> result = new ArrayList<String>();
		Cursor c = db.rawQuery("select name from tags", null);
		boolean mov = c.moveToFirst();
		while(mov){
			result.add(c.getString(0));
			mov = c.moveToNext();
		}
		c.close();
		return result;
	}

	public void deleteAllTags(){
		db.execSQL(TagSQLiteHelper.DROP_DB_TABLE);
		db.execSQL(TagSQLiteHelper.CREATE_DB_TABLE);
	}

	public void close(){
		db.close();
	}
}
