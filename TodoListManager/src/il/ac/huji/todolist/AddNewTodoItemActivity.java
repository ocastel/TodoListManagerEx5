package il.ac.huji.todolist;

import java.util.Date;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;

public class AddNewTodoItemActivity extends Activity {
	
	public void onCreate(Bundle unused) { 
		super.onCreate(unused); 
		setContentView(R.layout.add_new_todo_item_activity);
		// Once OK is clicked, take values from fields and return them
		findViewById(R.id.btnOK).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent result = new Intent();
					EditText edtText = (EditText) findViewById(R.id.edtNewItem);
					result.putExtra("title", edtText.getText().toString()); 
					DatePicker dp = (DatePicker) findViewById(R.id.datePicker);
					Date date= (Date) new Date(dp.getYear()-1900, dp.getMonth(), dp.getDayOfMonth());	
					result.putExtra("due", date);
					setResult(RESULT_OK, result); 
					finish();
				}
        	});

        findViewById(R.id.btnCancel).setOnClickListener(
        	new OnClickListener()
            {
    			@Override
    			public void onClick(View v) {
    				setResult(RESULT_CANCELED); 
    				finish();
    			}
            });

	}
}
	
	
