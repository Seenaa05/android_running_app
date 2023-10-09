package com.example.main;

import android.annotation.SuppressLint;

import android.app.Service;
import android.os.SystemClock;
import android.content.Context;
import java.util.Date;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;

import android.os.IBinder;
import android.content.ContentValues;
import android.util.Log;
import java.text.SimpleDateFormat;



public class LocationService extends Service {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final IBinder binder = new LocationServiceBinder();
    private long startTime = 0;
    private long stopTime = 0;


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener();
            locationListener.recordLocations = false;
            Log.d("mdp", "Location Service created");
        } catch (Exception e){
            Log.e("error", "Failure in starting location service");
        }


        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 3, 3, locationListener);
        } catch(SecurityException e) {

            Log.e("error", "No Permissions for GPS");
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,  flags, startId);



        return START_NOT_STICKY;
    }



    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        // user has closed the application so cancel the current journey and stop tracking GPS
        locationManager.removeUpdates(locationListener);
        locationListener = null;
        locationManager = null;



        Log.d("INFO", "Location Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected float getDistance() {
        return locationListener.getDistanceOfJourney();
    }


    protected void playJourney() {

        locationListener.newJourney();
        locationListener.recordLocations = true;
        stopTime = 0;
        startTime = SystemClock.elapsedRealtime();
    }

    /* Get the duration of the current journey */
    protected double getDuration() {
        if(startTime == 0) {
            return 0.0;
        }

        long endTime;
        endTime = SystemClock.elapsedRealtime();

        if(stopTime != 0) {
            // saveJourney has been called, until playJourney is called again display constant time
            endTime = stopTime;
        }

        long elapsedMilliSeconds;
        elapsedMilliSeconds = endTime - startTime;
        return elapsedMilliSeconds / 1000.0;
    }

    protected boolean currentlyTracking() {
        return startTime != 0;
    }

    /* Save journey to the database and stop saving GPS locations */
    protected void saveJourney() {
        Log.d("success", "Saving journey");
        ContentValues journeyData = new ContentValues();
        journeyData.put(JPC.J_DATE, getDateTime());
        journeyData.put(JPC.J_distance, getDistance());
        journeyData.put(JPC.J_DURATION, (long) getDuration());


        long journeyID = Long.parseLong(getContentResolver().insert(JPC.JOURNEY_URI, journeyData).getLastPathSegment());
        try {
            for (Location location : locationListener.getLocations()) {
                ContentValues locationData = new ContentValues();
                locationData.put(JPC.L_ALTITUDE, location.getAltitude());
                locationData.put(JPC.L_JID, journeyID);
                locationData.put(JPC.L_LONGITUDE, location.getLongitude());
                locationData.put(JPC.L_LATITUDE, location.getLatitude());

                getContentResolver().insert(JPC.LOCATION_URI, locationData);
            }
        }catch(Exception e){
            Log.e("Error", "Error adding items to database ");
        }


        Log.d("reset", "Resetting... ");
        locationListener.recordLocations = false;
        startTime = 0;
        stopTime = SystemClock.elapsedRealtime();
        locationListener.newJourney();

        Log.d("success", "Journey saved with id = " + journeyID);
    }






    private String getDateTime() {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return formatter.format(date);
    }

    public class LocationServiceBinder extends Binder {

        public float getDistance() {
            return LocationService.this.getDistance();
        }

        public double getDuration() {
            // get duration in seconds
            return LocationService.this.getDuration();
        }
        public void saveJourney() {
            LocationService.this.saveJourney();
        }
        public boolean currentlyTracking() {return LocationService.this.currentlyTracking();}

        public void playJourney() {
            LocationService.this.playJourney();
        }





    }
}

