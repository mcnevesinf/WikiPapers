package br.ufrgs.inf01059.wikipapers.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class NotesDAO {
	private static DBHelper dbHelper;

	public static List<Note> getNotes(Context context) {
		initContext(context);
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
				"SELECT _id, title, content, date FROM notes", null);

		List<Note> notes = new ArrayList<Note>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Note note = new Note(//
					cursor.getString(0),//
					cursor.getString(1),//
					cursor.getString(2),//
					new Date(cursor.getLong(3)));
			notes.add(note);
			cursor.moveToNext();
		}
		cursor.close();

		return notes;
	}

	public static Note getNote(Context context, int id) {
		initContext(context);
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
				"SELECT _id, title, content, date FROM notes WHERE _id = "
						+ String.valueOf(id) + ";", null);
		cursor.moveToFirst();
		Note note = new Note(//
				cursor.getString(0),//
				cursor.getString(1),//
				cursor.getString(2),//
				new Date(cursor.getLong(3)));
		cursor.close();
		return note;
	}
	
	public static long createNote(Context context, String title, String content) {
		
		initContext(context);
		long time = System.currentTimeMillis();
				
		ContentValues values = new ContentValues();

		values.put("title", title);
		values.put("content", content);
		values.put("date", time);
				
		return dbHelper.getWritableDatabase().insert("notes", null, values);
		
		
		
	}
	
	public static boolean editNote(Context context, int id, String title, String content){
		initContext(context);
		long time = System.currentTimeMillis();
				
		ContentValues values = new ContentValues();

		values.put("title", title);
		values.put("content", content);
		values.put("date", time);
				
		return dbHelper.getWritableDatabase().update("notes", values, "_id=?", new String[] { String.valueOf(id) }) > 0;
		
		
	}
	
	public static boolean deleteNote(Context context, int id) {
		initContext(context);

		return dbHelper.getWritableDatabase().delete("notes", "_id=?", new String[] { String.valueOf(id) }) > 0;
	}
	
	public static List<Note> searchNotes(Context context, String title, String orderBy, String limit){
		initContext(context);
		String selection = "title like ?";
		String[] selectionArgs = new String[]{"%" + title + "%"};
		Cursor cursor = dbHelper.getReadableDatabase().query(
				"notes", null, selection, selectionArgs, null, null, orderBy, limit);

		List<Note> notes = new ArrayList<Note>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Note note = new Note(//
					cursor.getString(0),//
					cursor.getString(1),//
					cursor.getString(2),//
					new Date(cursor.getLong(3)));
			notes.add(note);
			cursor.moveToNext();
		}
		cursor.close();

		return notes;
		
	}

	private static void initContext(Context context) {
		if (dbHelper == null) {
			dbHelper = new DBHelper(context);
		}
	}
}
