package com.pierre.mylocator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class SQLHelper extends SQLiteOpenHelper {
	
	public static abstract class AddressEntry implements BaseColumns {

    	public static final String TABLE_NAME = "address";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_ADDRESS1 = "address1";
        public static final String COLUMN_NAME_ADDRESS2 = "address2";
        public static final String COLUMN_NAME_ZIP = "zip";
        public static final String COLUMN_NAME_CITY = "city";
        public static final String COLUMN_NAME_STATE = "state";
        public static final String COLUMN_NAME_COUNTRY = "country";
        
    }
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + AddressEntry.TABLE_NAME + " (" +
        AddressEntry._ID + " INTEGER PRIMARY KEY," +
        AddressEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_LONGITUDE + REAL_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_ADDRESS1 + TEXT_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_ADDRESS2 + TEXT_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_ZIP + TEXT_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_CITY + TEXT_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_STATE + TEXT_TYPE + COMMA_SEP +
        AddressEntry.COLUMN_NAME_COUNTRY + TEXT_TYPE + 
        " )";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + AddressEntry.TABLE_NAME;
    
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AddressLocation.db";

	public SQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
	
	@Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
