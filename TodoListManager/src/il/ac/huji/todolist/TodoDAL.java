package il.ac.huji.todolist;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class TodoDAL {
	
	
	public SQLiteDatabase db;
	public Cursor displayCursor;
	
	
	public TodoDAL(Context context) {
		
		TodoDatabaseHelper helper = new TodoDatabaseHelper(context);	
		db = helper.getWritableDatabase();
		displayCursor = db.query("todoextended", new String[] {"_id","title","due","imagePath"},null,null,null,null,null);
		displayCursor.moveToFirst();

		Parse.initialize(context, context.getResources().getString(R.string.parseApplication),context.getResources().getString(R.string.clientKey));
		ParseUser.enableAutomaticUser();	
	}
	
	 
	public boolean insert(ITodoItem todoItem){
		String title = todoItem.getTitle();
		Date dueDate = todoItem.getDueDate();
		ParseObject parseObj = new ParseObject("todoextended"); 		
		parseObj.put("title", title);
		parseObj.put("imagePath", "default"); // default image
		if (dueDate == null)
		{
			parseObj.put("due",JSONObject.NULL);
		}
		else
		{
			parseObj.put("due", dueDate.getTime());
		}
		parseObj.put("imagePath",JSONObject.NULL);
		parseObj.saveInBackground();
						
		ContentValues todoTitleDate = new ContentValues(); 
		todoTitleDate.put("title", title); 
		todoTitleDate.put("imagePath","default");
		if (dueDate == null)
		{
			todoTitleDate.putNull("due");
		}
		else
		{
			todoTitleDate.put("due", dueDate.getTime()); 
		}
		long retVal = db.insert("todoextended", null, todoTitleDate);
		if (retVal == -1) {
			return false;
		}
		return true;
	}
	 
	public boolean update(ITodoItem todoItem){
		if (todoItem == null) return false;
		final ITodoItem finalTodoItem = todoItem;
		ParseQuery query = new ParseQuery("todoextended"); 
		query.whereEqualTo("title", todoItem.getTitle());
		query.findInBackground(new FindCallback() {
		@ Override
			public void done(List<ParseObject> matches, com.parse.ParseException e) { 
				if (e == null){
					if (!matches.isEmpty()){ //if there are any matches to the query
						matches.get(0).put("dueDate", finalTodoItem.getDueDate().getTime());
						matches.get(0).saveInBackground();
						//Assuming the list will include only 1 entry
					}
				}
				else { e.printStackTrace(); }
			}
		});
		ContentValues contentValues = new ContentValues();
		if(!(todoItem.getDueDate().equals(null))){ // Null date
			contentValues.put("due", todoItem.getDueDate().getTime());
		}
		else { contentValues.putNull("due"); }
		// DB update
		long retVal = db.update("todoextended",contentValues , "title=?", new String[]{todoItem.getTitle()});
		if (retVal != 1) {
			return false;
		}
		return true;		
	}
	 
	public boolean delete(ITodoItem todoItem){
		// Parse delete
		ParseQuery query = new ParseQuery("todoextended"); 
		query.whereEqualTo("title", todoItem.getTitle());
		query.findInBackground(new FindCallback()
			{
			@Override
			public void done(List<ParseObject> matches, com.parse.ParseException  e) { 
				if (e == null){
					if (!matches.isEmpty()){
						matches.get(0).deleteInBackground();
					}
				}
				else {
					e.printStackTrace();
				}
			}
		});
		// DB delete
		long retVal = db.delete("todoextended", "title = ?", new String[] {todoItem.getTitle() });
		if (retVal != 1) {
			return false;
		}
		displayCursor.requery();
		return true;		
	}
	
	// All was removed since it is not relevant for this exercise.
	
	/**
	 * Update a task image given it's (assumed unique) title on Parse and local SQLite.
	 */
	public boolean updateImage(String title ,String filePath) {
		final String finalFilePath = filePath;
	
		ParseQuery query = new ParseQuery("todoextended"); 
		query.whereEqualTo("title", title);
		query.findInBackground(new FindCallback() {
		@ Override
			public void done(List<ParseObject> matches, com.parse.ParseException e) { 
				if (e == null){
					if (!matches.isEmpty()){ //if there are any matches to the query, update an image
						Bitmap image = BitmapFactory.decodeFile(finalFilePath);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						image.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byte[] byteArray = stream.toByteArray();
						ParseFile file = new ParseFile(finalFilePath, byteArray);
						file.saveInBackground();
						matches.get(0).put("imagePath", file);
						matches.get(0).saveInBackground();
					}
				}
				else { e.printStackTrace(); }
			}
		});
		ContentValues contentValues = new ContentValues();
		contentValues.put("imagePath", finalFilePath);
		// Check if there is already an image for the task, and if so - 
		// delete the old image from SD card and update DB with the new image path
		String[] tableColumns = new String[] {"title","imagePath"};
		String whereClause = "title=?";
		String[] whereArgs = new String[] {title};
		Cursor curs = db.query("todoextended",tableColumns, whereClause, whereArgs, null, null, null);
		curs.moveToFirst();
		String path = curs.getString(1);
		if (!path.equals("default")) { // If the image is not the default one
			File file = new File(path);
			file.delete();			
		}
		curs.close();
		// Update the db with the new image path
		long retVal = db.update("todoextended",contentValues , "title=?", new String[]{title});
		if (retVal != 1) {
			return false;
		}
		displayCursor.requery();
		return true;		
	}
	
	// A raw query on the db, wehereArgs are possible but not mandatory
	Cursor dbRawQuery(String query, String[] whereArgs) {
		return this.db.rawQuery(query, whereArgs);
	}
	
	// Insert an id (twitter) to the db for later querying 
	void dbRawInsert(int id) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(new String("id"), id);
		db.insert("idList", null, contentValues);
	}
	
	Cursor getDisplayCursor() {
		return this.displayCursor;
	}

}