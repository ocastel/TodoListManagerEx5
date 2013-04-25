package il.ac.huji.todolist;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;

/**
 * An activity for adding thumbnails. Query Flickr with 18 images per page. Any click on "Search"
 * will search for the next page on Flickr, thus allowing fetching images by chunks, not overloading
 * the network, but aloowing extending the search.
 */
public class AddThumbnailActivity extends Activity {

	private ArrayList<Bitmap> bitmapList;
	private ArrayList<String> imageIdsList;
    private ImageAdapter imageAdapter;
    private GridView imagesGridView;
    private Bitmap selectedBitmap;
    private String selectedBitmapId;
    private Integer resultPage;
    private SearchImageAsyncTask searchImageAsyncTask;
    private String currentTag;
    private ProgressBar progressBar;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_thumbnail_activity);
        bitmapList = new ArrayList<Bitmap>();
        imageIdsList = new ArrayList<String>();
        imageAdapter = new ImageAdapter(this, bitmapList, imageIdsList);
        imagesGridView = (GridView) findViewById(R.id.grid_view);
        imagesGridView.setAdapter(imageAdapter);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        selectedBitmap = null;
        selectedBitmapId = null;
        resultPage = Integer.valueOf(1); // Used when querying, to allow querying for new reults
        currentTag = null;
        
        imagesGridView.setOnItemClickListener(new OnItemClickListener() {

        	// Once an image is clicked, save the bitmap and id to an instace variable for later use (on clicking "OK")
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	selectedBitmap = (Bitmap) imageAdapter.getItem(position);
            	selectedBitmapId = imageAdapter.getImageId(position);
            }
        });

        findViewById(R.id.btnSearch).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText query = (EditText) findViewById(R.id.edtThumbnailQuery);
				// Check if new search tag inserted - if so, stop clear last search results.
				if (currentTag != null && ! currentTag.equalsIgnoreCase(query.getText().toString())) {
					currentTag = query.getText().toString();
					resultPage = 1;
					bitmapList.clear();
					imageIdsList.clear();
					imageAdapter.notifyDataSetChanged();
					searchImageAsyncTask.cancel(true);
				}
				// Start a search with search tag. If the tag wasn't changed since last search, the results 
				// are added to the last ones, otherwise the list is empty (through the condition above)
				currentTag = query.getText().toString();
				searchImageAsyncTask = (SearchImageAsyncTask) new SearchImageAsyncTask(AddThumbnailActivity.this, bitmapList,imageIdsList,imageAdapter, progressBar).execute(query.getText().toString(),resultPage.toString());
				resultPage++;
			}
        });

        findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				searchImageAsyncTask.cancel(true);
				finish();
			}
        });

        
        findViewById(R.id.btnClear).setOnClickListener(new OnClickListener() {
			@Override
			// Clear lst results, but keep searching from last point searched.
			public void onClick(View v) {
				// Reset all data
				bitmapList.clear();
				imageIdsList.clear();
				imageAdapter.notifyDataSetChanged();
				selectedBitmap = null;
				selectedBitmapId = null;
				// Do not reset search page number (resultPage), to allow new results from Flickr.
			}
        });

        
        findViewById(R.id.btnOK).setOnClickListener(new OnClickListener() {
        	@Override
    		public void onClick(View v) {
        		// OK pressed without any image selected, do nothing
        		if (selectedBitmapId == null) {
        			setResult(RESULT_CANCELED);
        			finish();
        		}
        		// an image was selected, save and return it
        		Intent result = new Intent();
        		// Create file
        		String root = Environment.getExternalStorageDirectory().toString();
        		File myDir = new File(root + "/saved_images");    
        		myDir.mkdirs();
        		Date d = new Date();
        		String fname = selectedBitmapId+d.getTime()+".jpg";
        		File file = new File (myDir, fname);
        		if (file.exists ()) file.delete (); 
        		try { // Save image
        		       FileOutputStream out = new FileOutputStream(file);
        		       selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        		       out.flush();
        		       out.close();

        		} catch (Exception e) {
        		       e.printStackTrace();
        		       setResult(RESULT_CANCELED); // Set a failure code
        		       finish();
        		}
				result.putExtra("imagePath", file.getAbsolutePath());
				setResult(RESULT_OK, result); 
				finish();
        	}
        });

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.main, menu);
    	return true;
    }
    	
}
