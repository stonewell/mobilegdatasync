package com.angelstone.android.database;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GenericDatabaseHelper extends SQLiteOpenHelper {
	private List<Entity> mEntities = null;
	private int mDBVersion = 0;
	
	public GenericDatabaseHelper(Context context, String dbName, int dbVersion,
			List<Entity> entities) {
		super(context, dbName, null, dbVersion);
		
		mEntities = entities;
		mDBVersion = dbVersion;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Iterator<Entity> it = mEntities.iterator();
		
		while(it.hasNext()) {
			Entity entity = it.next();
			
			db.execSQL(entity.getCreateTableSQL());
			
			entity.initData(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < mDBVersion) {
			Iterator<Entity> it = mEntities.iterator();
			
			while(it.hasNext()) {
				Entity entity = it.next();
				
				db.execSQL("DROP TABLE IF EXISTS " + entity.getTableName() + ";");
			}
		}
		
		onCreate(db);
	}
}
