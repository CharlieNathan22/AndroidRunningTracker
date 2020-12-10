package com.example.runningtracker;

import android.net.Uri;

/**
 * ProviderContract for the Database to allow access with abstraction
 */
public class ProviderContract {

    final static String AUTHORITY = "com.example.runningtracker.MyContentProvider";
    final static Uri MY_URI = Uri.parse("content://com.example.runningtracker.MyContentProvider");

    public static final String _ID = "_id";
    public static final String AVERAGE_SPEED = "averageSpeed";
    public static final String MAXIMUM_SPEED = "maximumSpeed";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String NOTE = "notes";
}
