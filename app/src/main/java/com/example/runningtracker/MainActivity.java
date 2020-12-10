package com.example.runningtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static final int MAP_CODE = 1;
    static final int MYRUNS_CODE = 2;
    Receiver receiver;

    float averageSpeed;
    float topSpeed;
    String duration;
    String distance;

    Button startRun;
    Button stopRun;
    Button location;
    Button myRun;
    TextView averageSpeedText;
    TextView topSpeedText;
    TextView distanceText;
    TextView durationText;
    EditText noteText;

    LocationService.LocationServiceBinder binder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Main", "MainActivity created");

        //Connect the Buttons to the UI
        startRun = findViewById(R.id.StartRunButton);
        stopRun = findViewById(R.id.StopRunButton);
        stopRun.setEnabled(false);
        location = findViewById(R.id.LocationButton);
        myRun = findViewById(R.id.MyRunsButton);

        //Connect the textboxes to the UI
        distanceText = findViewById(R.id.DistanceText);
        durationText = findViewById(R.id.DurationText);
        averageSpeedText = findViewById(R.id.AverageSpeedText);
        topSpeedText = findViewById(R.id.MaxSpeedText);
        noteText = findViewById(R.id.NoteText);

        // Start the Location service
        IntentFilter filter = new IntentFilter("com.example.runningtracker.MAIN_RECEIVER");
        receiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        Intent trackingIntent = new Intent(this, LocationService.class);
        this.startService(trackingIntent);
        this.bindService(trackingIntent, serviceConnection, Service.BIND_AUTO_CREATE);

        //Check for permissions and gain permission for device location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.runningtracker.MAIN_RECEIVER");
        receiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        receiver = null;
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (LocationService.LocationServiceBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };

    /**
     * Displays the run data in the TextViews
     */
    private void showText(){
        Log.d("showText", "Updating text");
        durationText.setText("Time Elapsed: " + duration);
        distanceText.setText("Distance: " + distance);
        averageSpeedText.setText("Average Speed: " + averageSpeed + "m/s");
        topSpeedText.setText("Maximum speed:" + topSpeed + "m/s");
    }

    /**
     * Resets the TextViews when the run is finished
     */
    private void resetText(){
        Log.d("ResetText", "Reseting text");
        durationText.setText("Time Elapsed: 00:00:00");
        distanceText.setText("Distance: 0.000 km");
        averageSpeedText.setText("Average Speed: 0.0 m/s");
        topSpeedText.setText("Maximum speed: 0.0 m/s");
        noteText.setText("Note: ");
    }

    public void onShowLocationClicked(View v){
        Log.d("Main", "Show Location button clicked");
        Intent locationIntent = new Intent(this, MapActivity.class);
        startActivityForResult(locationIntent, MAP_CODE);
    }

    public void onShowMyRunsClicked(View v){
        Log.d("Main", "Show MyRuns button clicked");
        Intent myRunsIntent = new Intent(this, MyRuns.class);
        startActivityForResult(myRunsIntent, MYRUNS_CODE);
    }

    public void onStartButtonClicked(View v){
        Log.d("Main", "Start button clicked");
        startRun.setEnabled(false);
        stopRun.setEnabled(true);
        binder.start();
    }

    /**
     * When the run has stopped, reset the speed values
     */
    public void onStopButtonClicked(View v){
        startRun.setEnabled(true);
        stopRun.setEnabled(false);
        Intent serviceIntent = new Intent(this, LocationService.class);
        String testNote = noteText.getText().toString();
        serviceIntent.putExtra("note", noteText.getText().toString());
        this.startService(serviceIntent);
        binder.stop();
        averageSpeed = 0;
        topSpeed = 0;
        resetText();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(serviceConnection != null){
            unbindService(serviceConnection);
            serviceConnection = null;
        }
        binder.createNotification();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    //BroadcastReceiver class that is listening to the Location tracker
    public class Receiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if("com.example.runningtracker.MAIN_RECEIVER".equals(intent.getAction())){
                Log.d("Receiver", "Broadcast Receiver has received data");
                topSpeed = intent.getFloatExtra("maximumSpeed", 0);
                averageSpeed = intent.getFloatExtra("averageSpeed", 0);
                duration = intent.getStringExtra("duration");
                distance = intent.getStringExtra("distance");
                durationText.setText(duration);
                if(duration == null) duration = "00:00:00";
                if (distance == null) distance = "0.000";
                showText();
            }
        }
    }
}
