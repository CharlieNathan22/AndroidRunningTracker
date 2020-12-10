package com.example.runningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MyRuns extends AppCompatActivity {

    static final int SPECIFIC_RUN_ACTIVITY_CODE = 3;

    private ContentResolver resolver;

    private String orderBy = " DESC";
    private String param = ProviderContract.TIME;

    ListView list;
    Button newSortButton;
    Button oldSortButton;
    Button durationSortButton;
    Button distanceSortButton;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MyRuns", "MyRuns activity created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_runs);

        //Link up the buttons to the UI
        newSortButton = findViewById(R.id.NewSortButton);
        oldSortButton = findViewById(R.id.OldSortButton);
        durationSortButton = findViewById(R.id.DurationSortButton);
        distanceSortButton = findViewById(R.id.DistanceSortButton);
        backButton = findViewById(R.id.MyRunBackButton);
        list = findViewById(R.id.RunList);

        resolver = this.getContentResolver();
        loadData();
    }

    @Override
    protected void onResume(){
        Log.d("MyRuns", "Resuming MyRuns Activity");
        super.onResume();
        loadData();
    }

    // Sort list by longest time
    public void sortByDuration(View v){
        Log.d("MyRuns", "SortByDuration button clicked");
        param = ProviderContract.DURATION;
        orderBy = " DESC";
        loadData();
    }

    // Sort list by longest distance
    public void sortByDistance(View v){
        Log.d("MyRuns", "SortByDistance button clicked");
        param = ProviderContract.DISTANCE;
        orderBy = " DESC";
        loadData();
    }

    // sort list by most recent run
    public void sortByNewest(View v){
        Log.d("MyRuns", "SortByNewest button clicked");
        param = ProviderContract.TIME;
        orderBy = " DESC";
        loadData();
    }

    // sort list by oldest run
    public void sortByOldest(View v){
        Log.d("MyRuns", "SortByOldest button clicked");
        param = ProviderContract.TIME;
        orderBy = " ASC";
        loadData();
    }

    //When the back button is clicked
    public void onBackButtonClicked(View v){
        Intent intent = new Intent(this, MainActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Load data from Runs DB and populate list with a clickable interface to Specific Run activity
     */
    public void loadData(){
        String[] projection = new String[]{
                ProviderContract._ID,
                ProviderContract.DISTANCE,
                ProviderContract.DURATION,
                ProviderContract.DATE,
                ProviderContract.TIME,
                ProviderContract.AVERAGE_SPEED,
                ProviderContract.MAXIMUM_SPEED,
                ProviderContract.NOTE
        };
        String[] columns = new String[]{
                ProviderContract.DISTANCE,
                ProviderContract.DURATION,
                ProviderContract.DATE
        };
        int[] to = new int[]{
                R.id.DistanceRow,
                R.id.DurationRow,
                R.id.DateRow
        };

        //Cursor to populate list with runs from the DB
        final Cursor cursor = resolver.query(ProviderContract.MY_URI, projection, null,
                null, param + orderBy);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.db_row_layout, cursor,
                columns, to, 0);
        list.setAdapter(adapter);
        //When a record is clicked, load that run in SpecficiRunActivity page and pass ID of run
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int runId = cursor.getInt(cursor.getColumnIndex(ProviderContract._ID));
                Intent intent = new Intent(MyRuns.this, SpecificRunActivity.class);
                intent.putExtra("id", runId);
                startActivityForResult(intent, SPECIFIC_RUN_ACTIVITY_CODE);
            }
        });
    }
}
