package il.ac.huji.todolist;

import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

public class TodoListManagerActivity extends Activity {

    private SearchTweetAsyncTask searchTweetAsyncTask;
	private TodoDAL todoDal;
    private TodoDBAdapter todoDBAdapter;
    private ListView todoListView;    
    private static String titleAddedBitmap;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list_manager);
        // Set default values for preferences (hashtag = 'todolist') on first creation        
        PreferenceManager.setDefaultValues(this, R.layout.prefs, false);
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
        
        todoDal = new TodoDAL(this);
        todoListView = (ListView)findViewById(R.id.lstTodoItems);        
        registerForContextMenu(todoListView);
        // Set up adapter
        String[] from = {"title","due"}; 
		int[] to = {R.id.txtTodoTitle,R.id.txtTodoDueDate};
        todoDBAdapter = new TodoDBAdapter(this, R.layout.row, todoDal.displayCursor,from,to);
        todoListView.setAdapter(todoDBAdapter);
        // Prepare variables for Dialog
    	final ProgressBar tweetProgressBar = (ProgressBar)findViewById(R.id.tweetProgressBar);
    	final TodoDAL finalTodoDal = this.todoDal;
    	String hashtag = prefs.getString("hashtag", "todo");
    	// Check if this is the app first run on the device
        if (prefs.getBoolean("first_run", true))
        
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle("Do this action");
        	builder.setMessage("Would you like to try add 100 first relevant tweets with hashtag '"+hashtag+ "' into the todo list?");
        	final String finalHashTag = hashtag;
        	// Search Twitter with the definde hashtag.
        	builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				@Override
        		public void onClick(DialogInterface dialog, int which) {
                	searchTweetAsyncTask = (SearchTweetAsyncTask) new SearchTweetAsyncTask(TodoListManagerActivity.this, finalTodoDal ,tweetProgressBar).
                        	execute(finalHashTag,"100");
        	        dialog.dismiss();
        	    }
        	});
        	// Do nothing
        	builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
        	    @Override
        	    public void onClick(DialogInterface dialog, int which) {
        	    	dialog.dismiss();
        	    }
        	});
        	AlertDialog alert = builder.create();
        	alert.show();        	
        	// Set the "first run" flag to false
        	prefs.edit().putBoolean("first_run", false).commit(); 
        }        
        else {
        	// Not first run on the device, query Twitter for my definition of max results (200)
        	searchTweetAsyncTask = (SearchTweetAsyncTask) new SearchTweetAsyncTask(TodoListManagerActivity.this, this.todoDal ,tweetProgressBar).
        	execute(hashtag,"0");
        }
    }
    
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo info) {
		super.onCreateContextMenu(menu, v, info);
		getMenuInflater().inflate(R.menu.ctxmenu, menu);
		String title = todoDal.displayCursor.getString(1);
		menu.setHeaderTitle(title);
		if (!title.startsWith("Call ")) // If it is not a "Call" task, remove the calling option
			{
		 	menu.removeItem(R.id.menuItemCall);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.menuItemDelete:
			todoDal.delete(new ToDoTask(todoDal.displayCursor.getString(1),new Date()));
			break;
		case R.id.menuItemImage:
			titleAddedBitmap = todoDal.displayCursor.getString(1);
			Intent imageIntent = new Intent(this, AddThumbnailActivity.class);
			startActivityForResult(imageIntent, 43);
			break;
		case R.id.menuItemCall:
			Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ todoDal.displayCursor.getString(1).substring(5))); 
			startActivity(dial); 
		}
		return true;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.main, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case R.id.menuItemAdd:
    		Intent intent = new Intent(this, AddNewTodoItemActivity.class); 
    		startActivityForResult(intent, 42); 
    		break;
	    	case R.id.hashtagSet:
	    	Intent prefsIntent = new Intent(this, PrefsActivity.class);
	    	startActivity(prefsIntent);
	    	break;
    	}
	    return true;
    }
    	
    protected void onActivityResult(int reqCode, int resCode, Intent data) { 
    	if (resCode == RESULT_CANCELED)
    	{
    		return;
    	}
    	switch (reqCode) { 
    	case 42:
    		Date date = null;
    		if (data.getExtras().containsKey("due") )
    		{
        		date = (Date) data.getSerializableExtra("due");    			
    		}
    		String task = data.getStringExtra("title");
    		ToDoTask todoTask = new ToDoTask(task,date);
     		todoDal.insert(todoTask);
    		this.todoDal.getDisplayCursor().requery();

     	break;
     	
    	case 43:
    		String filePath = data.getStringExtra("imagePath");
    		todoDal.updateImage(titleAddedBitmap, filePath);
    	break;
    	}
    }

}
	