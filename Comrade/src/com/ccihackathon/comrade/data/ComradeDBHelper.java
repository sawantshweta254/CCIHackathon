package com.ccihackathon.comrade.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ComradeDBHelper extends SQLiteOpenHelper {

	private String dbName;
	private String dbPath;
	private Context context;
		
	public ComradeDBHelper(Context _context, String name, String path, CursorFactory factory) {
		super(_context, name, factory, 1);
		
		dbName = name;
		dbPath = path;
		context = _context;
		
		if(dbPath != null){
			try {
				createDataBase();
			} catch (Exception ioe) {
				throw new Error("Unable to create database");
			}
		}
		else
		{
			dbPath = "";
		}
	}

	
	private void createDataBase() {
		SQLiteDatabase mSqliteDatabase = null;
		
		if(databaseExists())
		{
		}
		else
		{
			mSqliteDatabase = this.getReadableDatabase();
			mSqliteDatabase.close();

			copyDataBase();
		}
	}


	@SuppressWarnings("resource")
	private void copyDataBase() {
		int length;
		byte[] buffer = new byte[1024];
		String databasePath = dbPath + dbName;

		try {
			InputStream databaseInputFile = context.getAssets().open(dbName+".sqlite");
			OutputStream databaseOutputFile = new FileOutputStream(databasePath);

			while ((length = databaseInputFile.read(buffer)) > 0) {
				databaseOutputFile.write(buffer, 0, length);
				databaseOutputFile.flush();
			}
			databaseInputFile.close();
			//databaseOutputFile.close();

		} catch (FileNotFoundException ex) {
			Log.e(getClass().getName(), "Exception", ex);
		} catch (IOException ex) {
			Log.e(getClass().getName(), "Exception", ex);
		}
	}


	private boolean databaseExists() 
	{
		File dbFile = new File(dbPath + dbName);
        
        if(!dbFile.exists())
        	return false;
        
		SQLiteDatabase mSqliteDatabase = null;
		try {
			String databasePath = dbPath + dbName;
			mSqliteDatabase = SQLiteDatabase.openDatabase(databasePath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException ex) {
			Log.e(getClass().getName(), "Exception", ex);
		}

		if (mSqliteDatabase != null) {
			mSqliteDatabase.close();
		}
		return mSqliteDatabase != null ? true : false;
	}


	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
