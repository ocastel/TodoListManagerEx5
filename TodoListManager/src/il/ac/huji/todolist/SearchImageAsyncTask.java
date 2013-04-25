package il.ac.huji.todolist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SearchImageAsyncTask extends AsyncTask<String , Integer, Integer> {

	
	private static final int RESULTS_PER_PAGE = 18; // Amount of images per "search" click
	private Context context;
	private ArrayList<Bitmap> bitmapsArrayList;
	private ImageAdapter imageAdapter;
	private ArrayList<String> imageIdsList;
	private ProgressBar progressBar;
	
	
	public SearchImageAsyncTask(Context context, ArrayList<Bitmap> bitmapsArrayList, ArrayList<String> imageIdsList, ImageAdapter imageAdapter, ProgressBar progressBar){
		this.context = context;
		this.bitmapsArrayList = bitmapsArrayList;
		this.imageAdapter = imageAdapter;
		this.imageIdsList = imageIdsList;
		this.progressBar = progressBar;
		progressBar.setMax(RESULTS_PER_PAGE);
		}


	@Override
	protected Integer doInBackground(String... params) {
		ArrayList<String> results = null;
		try {
			// Query Flickr for the tag, and get a list of URLs of the images (thumbnail .jpg format)
			results = getQueryResults(params[0],params[1],this.imageIdsList);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// Iterate over the image URLs and download each bitmap.
		if (results != null && results.size() > 0) { // Verify there are any results
			publishProgress(0); // Show progress bar
			for (int i = 0; i < results.size() && !isCancelled() ; ++i) {
				Bitmap bitmapTemp = null;
				try {
					// Download the image from the URL
					bitmapTemp = BitmapFactory.decodeStream((InputStream)new URL(results.get(i)).getContent());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Verify the task wasn't cancelled during download
				if (!isCancelled()) {
					this.bitmapsArrayList.add(bitmapTemp);
					publishProgress(i+1);
				}
			}
			return results.size();
		}
		if (results == null) {
			return -1; // Let the user (through Toast) jnow there was a connection error
		}
		return 0; // If we got here, we got no results for the search
	}
	
	@Override
	protected void onPreExecute() {
		// Nothing to do
	}

	//This function receives the progress information published using publishProgress
	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] == 0) { // Got results, show progress bar
			progressBar.setProgress(0);
			progressBar.setVisibility(View.VISIBLE);			
		}
		else {
			progressBar.setProgress(values[0]); // The list was updated, update progress bar accordingly
			imageAdapter.notifyDataSetChanged();
			super.onProgressUpdate(values);			
		}
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if (result == 0) {
			Toast.makeText(this.context, "No results found", Toast.LENGTH_LONG).show();			
		}
		else if (result == -1) {
			Toast.makeText(this.context, "An error occured", Toast.LENGTH_LONG).show();			
		}
		else { // Images were downloaded successfully, remove progress bar.
			progressBar.setVisibility(View.GONE);
		}
	}
	
	// Read a stream and return as String. Adopted for outer source.
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
	 * Get a String and query flickr, with the String as tag. Use no other search parameters.
	 * Return an ArrayList of the returned images urls.
	 */
	private static ArrayList<String> getQueryResults(String tag, String page, ArrayList<String> imageIdsList) throws IOException, JSONException {
		ArrayList<String> urls = new ArrayList<String>();
		String queryString = "http://api.flickr.com/services/rest/?method=flickr.photos.search&safe_search=1&per_page="+RESULTS_PER_PAGE+"&format=json&nojsoncallback=1&page="+page+"&api_key="+"a5e244369cc9ebaf207d5cd7fe85450e"+"&tags="+tag;
		URL url = new URL(queryString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		String response = readStream(conn.getInputStream());
		JSONObject json = new JSONObject(response);	
		JSONObject photosObj = (JSONObject) json.get("photos");
		JSONArray arr = photosObj.getJSONArray("photo");
		for (int i = 0 ; i<arr.length() ; ++i) {
			JSONObject itemObj = (JSONObject) arr.get(i);			
			urls.add("http://farm"+itemObj.getInt("farm")+".staticflickr.com/"+itemObj.getString("server")+"/"+itemObj.getString("id")+"_"+itemObj.getString("secret")+"_s.jpg");
			imageIdsList.add(tag+itemObj.getString("id"));
		}
		return urls;
	}
	
	
}
