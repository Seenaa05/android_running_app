package com.example.main;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import java.util.Calendar;


import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import java.util.Date;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
public class RecordJourney extends AppCompatActivity {
    private LocationService.LocationServiceBinder locationService;

    private TextView distanceText;
    private TextView avgSpeedText;
    private static final String TAG = "MyActivity";
    private Button playButton;
    private Button stopButton;
    private TextView durationText;

    private static final int PERMISSION_GPS_CODE = 1;

    // will poll the location service for distance and duration
    private  Handler postBack = new Handler();

    private ServiceConnection lsc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            locationService = (LocationService.LocationServiceBinder) iBinder;

            initButtons();

            new Thread(() -> {
                Log.d("INFO", "Service connected");
                while (locationService != null) {
                    // get the distance and duration from the surface
                    float d;
                    d = (float) locationService.getDuration();
                    long duration = (long) d;  // in seconds
                    long hours = duration / 3600;
                    long minutes = (duration % 3600) / 60;
                    long seconds = duration % 60;
                    float distance;
                    distance = locationService.getDistance();

                    float avgSpeed = 0;
                    if (d != 0) {
                        avgSpeed = distance / (d / 3600);
                    }

                    @SuppressLint("DefaultLocale") final String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    @SuppressLint("DefaultLocale") final String dist = String.format("%.2f ", distance);
                    @SuppressLint("DefaultLocale") final String avgs = String.format("%.2f ", avgSpeed);

                    postBack.post(() -> {
                        // post back changes to UI thread
                        distanceText.setText(dist);
                        durationText.setText(time);
                        avgSpeedText.setText(avgs);

                    });

                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationService = null;
        }
    };

    // whenever activity is reloaded while still tracking a journey (if back button is clicked for example)
    private void initButtons() {
        // if currently tracking then enable stopButton and disable startButton
        if(locationService != null && locationService.currentlyTracking()) {
            stopButton.setEnabled(true);
            playButton.setEnabled(false);

        }
        // no permissions means no buttons
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopButton.setEnabled(false);
            playButton.setEnabled(false);
        }

     else {
            stopButton.setEnabled(false);
            playButton.setEnabled(true);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_journey);


        playButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        stopButton.setEnabled(false);
        playButton.setEnabled(false);
        distanceText = findViewById(R.id.distanceText);
        durationText = findViewById(R.id.durationText);

        avgSpeedText = findViewById(R.id.avgSpeedText);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
            } else {
                DialogFragment modal = NoPermissionDialogue.newInstance();
                modal.show(getSupportFragmentManager(), "Permissions");
            }

        }


        // start the service so that it persists outside of the lifetime of this activity
        // and also bind to it to gain control over the service
        startService(new Intent(this, LocationService.class));
        bindService(
                new Intent(this, LocationService.class), lsc, Context.BIND_AUTO_CREATE);
    }
    public void onClickBack(View v){
        Intent main = new Intent(RecordJourney.this, MainActivity.class);
        startActivity(main);
    }
    public void onClickPlay(View view) {

        playButton.setEnabled(false);
        stopButton.setEnabled(true);
        Log.d(TAG,"Play clicked");
        locationService.playJourney();

    }

    public void onClickStop(View view) {

        playButton.setEnabled(false);
        stopButton.setEnabled(false);
        // save the current journey to the database
        float distance = locationService.getDistance();
        Log.d(TAG,"Stop clicked");
        locationService.saveJourney();




        DialogFragment modal = FinishedTrackingDialogue.newInstance(String.format("%.2f KM", distance));
        modal.show(getSupportFragmentManager(), "Finished");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();



        // unbind to the service (if we are the only binding activity then the service will be destroyed)
        if(lsc != null) {
            unbindService(lsc);
            lsc = null;
        }
    }

    public static class FinishedTrackingDialogue extends DialogFragment {
        public static  FinishedTrackingDialogue newInstance(String distance) {
            Bundle savedInstanceState;
            savedInstanceState = new Bundle();
            savedInstanceState.putString("distance", distance);
            FinishedTrackingDialogue frag;
            frag = new FinishedTrackingDialogue();
            frag.setArguments(savedInstanceState);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Your Journey has been saved. You ran a total of " + getArguments().getString("distance") )
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("Info","Saved journey");
                            getActivity().finish();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


    // PERMISSION THINGS

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(reqCode, permissions, results);
        if (reqCode == PERMISSION_GPS_CODE) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                initButtons();
            } else {
                // permission denied, disable GPS tracking buttons
                playButton.setEnabled(false);
                stopButton.setEnabled(false);

            }
        }
    }


    public static class NoPermissionDialogue extends DialogFragment {
        private Bundle savedInstanceState;

        public static  NoPermissionDialogue newInstance() {
            return new NoPermissionDialogue();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.savedInstanceState = savedInstanceState;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("GPS is required to track your journey!")
                    .setPositiveButton("Enable GPS", (dialog, id) -> {
                        Log.d("INFO","user agreed to enable GPS");
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("INFO","User rejected permissions");
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


}

