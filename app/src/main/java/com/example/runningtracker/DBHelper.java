package com.example.runningtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context){
        super(context, "RunningDB", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE RunningTrackerDB(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date VARCHAR(128)," +
                "time INTEGER," +
                "distance VARCHAR(128)," +
                "duration VARCHAR(128)," +
                "averageSpeed FLOAT," +
                "maximumSpeed FLOAT," +
                "notes VARCHAR(128)" +
                ");");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS myDB");
        onCreate(db);
    }
}
