package il.ac.huji.todolist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TodoDBAdapter extends SimpleCursorAdapter{

    private Context myContext;
    private int myLayout;
    public Cursor myCursor;
    
	public TodoDBAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to) {
		super(context, layout, c, from, to);
        this.myContext=context;
        this.myLayout=layout;
        this.myCursor=c;
		
	}
	
	@Override
	public View getView( int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView txtTitle = (TextView)view.findViewById(R.id.txtTodoTitle);
		TextView txtDate = (TextView)view.findViewById(R.id.txtTodoDueDate);
		ImageView imageView = (ImageView)view.findViewById(R.id.thumbnail);
		txtTitle.setText(" "+myCursor.getString(1));
		Date date = null;
		try { //Check if due date has past and set text color accordingly
			date = new Date(myCursor.getLong(2));
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
			txtDate.setText(sdf.format(date));		
			Date now = new Date();
			// Set text color
			if (now.after(date))
			{
				txtTitle.setTextColor(Color.RED);
				txtDate.setTextColor(Color.RED);
			}
			else
			{
				txtTitle.setTextColor(Color.BLACK);
				txtDate.setTextColor(Color.BLACK);
			}
		}
			catch(Exception e) {
			txtDate.setText("No due date");
		}
		// Override default image using path if such exist, otherwise use image from draeble
		String imagePath = myCursor.getString(3);
		if (!imagePath.equals("default")) {
			Bitmap image;
			image = BitmapFactory.decodeFile(imagePath);
			imageView.setImageBitmap(image);			
		}
		else {
			imageView.setImageDrawable(this.myContext.getResources().getDrawable(R.drawable.ic_launcher));
		}
		return view;
	}

}
