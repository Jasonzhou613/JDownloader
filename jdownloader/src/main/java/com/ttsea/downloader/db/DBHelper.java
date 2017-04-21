package com.ttsea.downloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.ttsea.downloader.download.JDownloadLog;


class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "jdownload.db";
    private Context mContext;

    public DBHelper(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
    }

    public static SQLiteDatabase getReadableDatabase(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        JDownloadLog.d(TAG, "getReadableDatabase");

        return db;
    }

    public static SQLiteDatabase getWritableDatabase(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        JDownloadLog.d(TAG, "getWritableDatabase");

        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        DBConstants.createTables(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        JDownloadLog.d(TAG, "oldVersion:" + oldVersion + ", newVersion:" + newVersion);

    }
}
