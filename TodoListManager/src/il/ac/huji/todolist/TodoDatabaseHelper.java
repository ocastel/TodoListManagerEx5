package il.ac.huji.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

	public TodoDatabaseHelper(Context context) {
		super(context, "todoextend_db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Table for the tasks (title, due date, image path) 
		db.execSQL("create table todoextended ( _id integer primary key autoincrement,"
				+  " title text, due integer, imagePath text);");
		// DB for the Flickr seen ids
		db.execSQL("create table idList ( _id integer primary key autoincrement,"
				+  " id integer);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Nothing to do.
	}

}