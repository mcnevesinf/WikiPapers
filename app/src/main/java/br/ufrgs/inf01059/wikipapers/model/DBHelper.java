package br.ufrgs.inf01059.wikipapers.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String NOTE_TABLE_NAME = "notes";

	public DBHelper(Context context) {
		super(context, "wikinotes", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		Long unix_time = Calendar.getInstance().getTimeInMillis();
		String sqlNotesCreate = "CREATE TABLE IF NOT EXISTS notes (" //
				+ "_id integer primary key autoincrement," //
				+ "title text," //
				+ "content text,"//
				+ "date integer);";
		db.execSQL(sqlNotesCreate);
		String sqlPopulateSample1 = "INSERT into "
                + NOTE_TABLE_NAME
                + " (title, content, date) VALUES"
                + " ('Start here', 'Click on the link to open the target notes', " + unix_time + ");";
		db.execSQL(sqlPopulateSample1);
		String sqlPopulateSample2 = "INSERT into notes (title, content, date) VALUES "
                + "('Target', 'Welcome. You managed to open the targeted note! Ande here is an extra link', " + unix_time + ");";
		db.execSQL(sqlPopulateSample2);
		String sqlPopulateSample3 = "INSERT into notes (title, content, date) VALUES "
                + "('Extra', 'And there is an extra note!', " + unix_time + ");";
		db.execSQL(sqlPopulateSample3);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Create upgrade methods, when needed
		// http://stackoverflow.com/questions/14419358/confusion-how-does-sqliteopenhelper-onupgrade-behave-and-together-with-impor
		
	}

}
