package il.ac.huji.todolist;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * A simple image adapter from a Bitmap list. 
 */
public class ImageAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Bitmap> bitmapList;
	private ArrayList<String> idList;

    // Constructor
    public ImageAdapter(Context c, ArrayList<Bitmap> bitmapList, ArrayList<String> idList){
        this.context = c;
        this.bitmapList = bitmapList;
        this.idList = idList;
    }

    public int getCount() {
        return this.bitmapList.size();
    }

    public Object getItem(int position) {
        return this.bitmapList.get(position);
    }

    public String getImageId(int position) {
    	return this.idList.get(position);
    }
    
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {         
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmapList.get(position));        	
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(60, 60));
        return imageView;
    }

}
