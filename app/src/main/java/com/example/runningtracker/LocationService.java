package com.example.runningtracker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocationService class represents the tracking service running in the foreground
 * The data is obtained and sent to a listener to be broadcasted via binder
 */
public class LocationService extends Service {

    private ContentResolver resolver;
    private LocationServiceBinder serviceBinder = new LocationServiceBinder();
    int NOTIFICATION_ID = 1;
    LocationManager locationManager;
    MyLocationListener listener;

    float totalDuration;
    String duration;
    float totalDistance;
    float distance;
    float averageSpeed;
    float speed;
    float alreadyMax;
    int differentLocations = 0;
    long time = 0;
    String date;
    String stringDistance;
    String runNote;
    float topSpeed = 0;
    float tempSpeed = 0;

    Location previousLocation = null;
    double[] locations = new double[2];

    @Override
    public void onCreate() {
        Log.d("LocationService", "LocationService created");
        super.onCreate();
        resolver = this.getContentResolver();

        //Create a listener, check for permissions and use the manager to get location update
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5,
                5, listener);
        stopGPS();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        runNote = intent.getStringExtra("note"); //Take the details of the note from the Main Activity
        return START_STICKY;
    }

    /**
     * Creates a new location manager and locationUpdateRequest
     */
    private void startGPS() {
        Log.d("LocationService", "Starting GPS");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, listener);
    }

    /**
     * clears the location updates from the listener
     */
    private void stopGPS(){
        Log.d("LocationService", "Stopping GPS");
        locationManager.removeUpdates(listener);
    }

    /**
     * Creates a notification in android using notification manager,builder
     */
    private void createNotification(){
        Log.d("LocationService", "Creating Notification");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String CHANNEL_ID = "200";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Running Tracker", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Tracking");
            notificationManager.createNotificationChannel(notificationChannel);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Running Tracker")
                    .setContentText("Tracking")
                    .setContentIntent(pendingIntent);
            startForeground(NOTIFICATION_ID, builder.build());
        }

    }

    /**
     * ends the notification service in the foreground
     */
    private void stopNotification(){
        Log.d("LocationService", "Stopping Notifications");
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        locationManager.removeUpdates(listener);
        return true;
    }

    /**
     * Binder subclass to manage the location service
     */
    public class LocationServiceBinder extends Binder{
        public void start(){
            LocationService.this.createNotification();
            LocationService.this.startGPS();
        }

        public void stop(){
            date = getDate(time);
            LocationService.this.stopNotification();
            LocationService.this.stopGPS();
            addRun();
        }

        /**
         * Add the run details to the Database
         */
        private void addRun(){
            ContentValues values = new ContentValues();
            values.put(ProviderContract.DATE, date);
            values.put(ProviderContract.TIME, time);
            values.put(ProviderContract.DISTANCE, stringDistance);
            values.put(ProviderContract.DURATION, duration);
            values.put(ProviderContract.AVERAGE_SPEED, averageSpeed);
            values.put(ProviderContract.MAXIMUM_SPEED, topSpeed);
            values.put(ProviderContract.NOTE, runNote);
            resolver.insert(ProviderContract.MY_URI, values);
        }

        /**
         *
         * Converts the date logged when the run is finished to DateTimeFormat
         * @return String that represents the date in the correct DateTimeFormat
         */
        private String getDate(long epoch){
            return DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss")
                    .format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch/1000),
                            ZoneId.of("Europe/London")));
        }

        public void createNotification(){
            LocationService.this.createNotification();
        }
    }

    /**
     * Location listener to track when location has changed and calculate values to send to back to main and map page
     */
    public class MyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            Log.d("LocationService", location.getLatitude() + " " + location.getLongitude());
            // Get the current Latitude and Longitude, convert it to distance and send it broadcaster
            locations[0] = location.getLatitude();
            locations[1] = location.getLongitude();
            broadcastMap();

            //Calculate distance and duration values
            time = location.getTime();
            differentLocations = differentLocations + 1;
            if(previousLocation != null){
                totalDuration +=  (location.getTime() - previousLocation.getTime())/1000;
                int roundDuration = Math.round(totalDuration);
                duration = convertDuration(roundDuration);
                distance = previousLocation.distanceTo(location) / 1000;
                totalDistance += roundValues(3, distance);
                Log.d("LocationService", String.valueOf(roundValues(3,distance)));
                stringDistance = totalDistance + " kilometers";
            } else{
                totalDuration = 0;
                totalDistance = 0;
            }

            //Calculate speed values
            if(previousLocation != null){
                speed = (previousLocation.distanceTo(location)) / ((location.getTime() - previousLocation.getTime()) /1000);
            } else {
                speed = 0;
            }
            tempSpeed += speed;
            averageSpeed = roundValues(1,tempSpeed/differentLocations);
            topSpeed = getMaximumSpeed(speed);
            previousLocation = location;
            broadcastMain();
        }

        /**
         * Convert the duration from int seconds into Date Time Format of hours/minutes/seconds
         */
        private String convertDuration(int secs){
            int hours = secs/3600;
            int minutes = (secs % 3600) / 60;
            int seconds = secs % 60;
            Log.d("LocationService","hours: " + hours + "---minutes: " + minutes + "---seconds: " + seconds);
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        /**
         * Round the distance and speed values to the decimal place entered
         */
        private float roundValues(int decimal, float num){
            return new BigDecimal(num).setScale(decimal, BigDecimal.ROUND_HALF_UP).floatValue();
        }

        /**
         * calculate Maximum speed by comparing current speed with maximum value
         */
        private float getMaximumSpeed(float speed){
            if(alreadyMax == 0){
                topSpeed = speed;
                alreadyMax = speed;
            } else {
                alreadyMax = speed;
                if(topSpeed < alreadyMax){
                    topSpeed = alreadyMax;
                }
            }
            return roundValues(1, topSpeed);
        }

        /**
         * Broadcast data back to the receiver for use in Map activity
         */
        public void broadcastMap(){
            Intent intent = new Intent();
            intent.setAction("com.example.runningtracker.MAP_RECEIVER");
            intent.putExtra("locations", locations);
            sendBroadcast(intent);
        }

        /**
         * Broadcast data back to the receiver for use in Main activity
         */
        public void broadcastMain(){
            Intent intent = new Intent();
            intent.setAction("com.example.runningtracker.MAIN_RECEIVER");
            intent.putExtra("distance", stringDistance);
            intent.putExtra("duration", duration);
            intent.putExtra("maximumSpeed", topSpeed);
            intent.putExtra("averageSpeed", averageSpeed);
            LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("LocationService", "Provider disabled");
            locationManager = null;
            listener = null;
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
    }
}
