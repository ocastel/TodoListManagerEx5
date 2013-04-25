package il.ac.huji.todolist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.InputFilter.LengthFilter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SearchTweetAsyncTask extends AsyncTask<String , Integer, Integer> {

	
	private Context context;
    private TodoDAL todoDal;
    private ProgressBar progressBar;
	
	public SearchTweetAsyncTask(Context context, TodoDAL todoDal, ProgressBar progressBar){
		this.context = context;
		this.todoDal = todoDal;
		this.progressBar = progressBar;
		}


	@Override
	protected Integer doInBackground(String... params) {	
		ArrayList<ToDoTask> results = null;
		try {
			results = getQueryResults(params[0], Integer.valueOf(params[1]));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Integer call_result = null;
		if (results != null) {
			int size = results.size();
			if (size > 0) {
				publishProgress(new Integer[]{results.size(),1}); // Update progress - set progress to 0 and max to size
				for (int i = 0; i < results.size() ; ++i) {
					this.todoDal.insert(results.get(i));
					publishProgress(new Integer[] {i+1,0});
				}
			}
			call_result = size;
		}
		return call_result;
	}
	
	@Override
	protected void onPreExecute() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		String hashtag = prefs.getString("hashtag", "todo");
		Toast.makeText(this.context, "Will attempt fetching tasks from Twitter with hashtag '"+hashtag+"'" , Toast.LENGTH_LONG).show();

	}

	//This function receives the progress information published using publishProgress
	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[1] == 1) { //Set the progress bar
			progressBar.setMax(values[0]);
			progressBar.setVisibility(View.VISIBLE);
		}
		else {
			this.todoDal.getDisplayCursor().requery();
			progressBar.setProgress(values[0]);
		}
		super.onProgressUpdate(values);			
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		progressBar.setVisibility(View.GONE);
		String msg;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		String hashtag = prefs.getString("hashtag", "todo");
		if (result == null) {
			msg = "Could not connect to Twitter, please check your internet connection!";
		}
		else if (result == 0) {
			msg = "No tweets with hashtag '"+hashtag+"' were found!";
		}
		else {
			msg = "Added "+result+" tasks from twitter with hashtag '"+hashtag+"'!";
		}
		Toast.makeText(this.context, msg , Toast.LENGTH_LONG).show();	
	}
	
	private static String readStream(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer buffer = new StringBuffer();
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			buffer.append(line);
			buffer.append('\n');
		}
		return buffer.toString();
	}
		
		
		
	/*
	 * Get a String and query tweeter, with the String as hashtag.
	 * Parse the results into a list of TodoItems.
	 */
	private ArrayList<ToDoTask> getQueryResults(String hashtag, int limit) throws IOException, JSONException {
		ArrayList<ToDoTask> tasks = new ArrayList<ToDoTask>();
		// To avoid twitter query limitations, query 4 times for 50 results, to get total of 200 with good odds.
		for (int i =1; i<=20; ++i) { // i is page number to query
			String queryString = "http://search.twitter.com/search.json?rpp=10;page="+i+";q="+hashtag;
			URL url = new URL(queryString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			String response = readStream(conn.getInputStream());
			JSONObject json = new JSONObject(response);	
			JSONArray arr = json.getJSONArray("results");
			if (arr.length() == 0) {
				break;
			}
			for (int j = 0 ; j<arr.length() ; ++j) {
				if (limit == 100 && tasks.size() == 100) {
					break; // Check if iterated first 100 results
				}
				JSONObject itemObj = (JSONObject) arr.get(j);
				Integer id = itemObj.getInt("id");
				
				Cursor curs = todoDal.dbRawQuery("SELECT id FROM idList WHERE id=?", new String[]{id.toString()});
				curs.moveToFirst();
				if (curs.getCount() == 0) {
					String task = itemObj.getString("text");
					// Use the "created_at" tweet field as new task duedate
					String date = itemObj.getString("created_at");
					Date dueDate = parseDate(date);
					ToDoTask todoTask = new ToDoTask(task, dueDate);
					tasks.add(todoTask);
					this.todoDal.dbRawInsert(id);
				}
				curs.close();
			}
		}
		return tasks;
	}
	
	Date parseDate(String dateString){ 
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss ZZZZZ", Locale.ENGLISH);
		dateFormat.setLenient(false);
		Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (Exception e) {
			return null;
		}
		return date;
	}
}
