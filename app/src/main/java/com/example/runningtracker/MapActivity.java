package com.example.runningtracker;

import androidx.fragment.app.FragmentActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    MapReceiver receiver = new MapReceiver();
    private GoogleMap map;
    LatLng lng;

    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Map", "Map activity started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Link up back button
        backButton = findViewById(R.id.MapBackButton);

        //Load up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.GPSMap);
        mapFragment.getMapAsync(this);
        IntentFilter filter = new IntentFilter("com.example.runningtracker.MAP_RECEIVER");
        registerReceiver(receiver, filter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onResume(){
        Log.d("Map Activity", "Resuming map Receiver");
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("com.example.runningtracker.MAP_RECEIVER");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause(){
        Log.d("Map Activity", "Pausing Map");
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onBackButtonClicked(View v){
        Intent intent = new Intent(this, MainActivity.class);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    /**
     * Broadcast receiver for the map. Updating user with the current position on the GPS
     */
    public class MapReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if("com.example.runningtracker.MAP_RECEIVER".equals(intent.getAction())){
                // Get current position from GoogleMap library and set the zoom and location of the visible map
                double[] location = intent.getDoubleArrayExtra("locations");
                lng = new LatLng(location[0], location[1]);
                map.clear();
                // positions the camera and zoom to the users location
                map.moveCamera(CameraUpdateFactory.newLatLng(lng));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lng, 15));
                map.addMarker(new MarkerOptions().position(lng));
            }
        }
    }
}
