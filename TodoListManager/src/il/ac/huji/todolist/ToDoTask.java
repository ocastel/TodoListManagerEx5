package il.ac.huji.todolist;

import java.util.Date;

public class ToDoTask implements ITodoItem{
	private String _task;
	private  Date _dueDate;
	private String _imagePath;
	

	public ToDoTask(String task, Date date) {
		this._task = task;
		this._dueDate = date;
		this._imagePath = null;
	}

	@Override
	public String getTitle() {
		return _task;
	}

	@Override
	public Date getDueDate() {
		return _dueDate;
	}
	
	public String getImagePath() {
		return _imagePath;
	}
}
