package com.example.runningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SpecificRunActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private ContentResolver contentResolver;
    int id;

    Button backButton;
    Button deleteButton;
    TextView averageSpeedText_run;
    TextView maximumSpeedText_run;
    TextView distanceText_run;
    TextView durationText_run;
    TextView dateText_run;
    TextView noteText_run;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_run);

        //Link text objects and Buttons to variables
        backButton = findViewById(R.id.SpecificRunBackButton);
        deleteButton = findViewById(R.id.DeleteRunButton);
        dateText_run = findViewById(R.id.DateTimeRunText);
        distanceText_run = findViewById(R.id.DistanceRunText);
        durationText_run = findViewById(R.id.DurationRunText);
        averageSpeedText_run = findViewById(R.id.AverageRunText);
        maximumSpeedText_run = findViewById(R.id.MaximumRunText);
        noteText_run = findViewById(R.id.NoteRunText);

        //Link up content Resolver to the database using DBHelper
        contentResolver = this.getContentResolver();
        dbHelper = new DBHelper(SpecificRunActivity.this);
        db = dbHelper.getWritableDatabase();
        // Obtain ID of run in DB
        id = getIntent().getIntExtra("id", 0);

        getRunRecord();
    }

    /**
     * Query the database for a specific run
     */
    private void getRunRecord(){
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

        // Create a cursor to get data from database
        final Cursor c = contentResolver.query(ProviderContract.MY_URI, projection,
                ProviderContract._ID + " = " + id, null, null);
        if(c.getCount() != 0 ){
            c.moveToFirst();
            // Display the data in TextViews
            distanceText_run.setText("Distance: " + c.getString(c.getColumnIndex(ProviderContract.DISTANCE)));
            durationText_run.setText("Duration: " + c.getString(c.getColumnIndex(ProviderContract.DURATION)));
            averageSpeedText_run.setText("Average speed: " + c.getFloat(c.getColumnIndex(ProviderContract.AVERAGE_SPEED)));
            maximumSpeedText_run.setText("Max speed: " + c.getFloat(c.getColumnIndex(ProviderContract.MAXIMUM_SPEED)));
            dateText_run.setText(c.getString(c.getColumnIndex(ProviderContract.DATE)));
            noteText_run.setText(c.getString(c.getColumnIndex(ProviderContract.NOTE)));
        }
    }

    // Returns to main MyRuns page
    public void onBackButtonClicked(View v){
        Intent intent = new Intent(this, SpecificRunActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    //Deletes specific run from DB and returns to main MyRuns page with updated records
    public void onDeleteRunButtonClicked(View v){
        contentResolver.delete(ProviderContract.MY_URI, ProviderContract._ID + " = " + id, null);
        Intent intent = new Intent(this, SpecificRunActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

}
