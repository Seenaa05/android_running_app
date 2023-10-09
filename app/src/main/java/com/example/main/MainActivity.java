package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_GPS_CODE = 1;
    private static final int PERMISSION_COAL_GPS_CODE = 2;
    private static final String TAG = "MyActivity";
    private static final String TAG2 = "Location";
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private TextView weatherText;
    private TextView weatherText2;
    private int userLat;
    private int userLong;
    private LocationManager locationManager;

    @SuppressLint({"MissingPermission", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("permission","No permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
                //temp values to get weather data until gps gets actual data
            userLat = 52;
            userLong = -1;

        }

        else {
            Log.d("permission","permission granted");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
            @SuppressLint("MissingPermission") Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            try {
                userLat = (int) loc.getLatitude();
                userLong = (int) loc.getLongitude();
            }catch(Exception e){
                userLat = 52;
                userLong = -1;

            }
        }




        getData();
        weatherText = findViewById(R.id.weatherCelsius);
        weatherText2 = findViewById(R.id.weatherDescription);

    }
   private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            userLat = (int) location.getLatitude();
            userLong = (int) location.getLongitude();
            Log.i(TAG2, String.valueOf(userLat));
            Log.i(TAG2, String.valueOf(userLong));
            getData();

        }

    };

    public void getData(){
        mRequestQueue = Volley.newRequestQueue(this);
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+userLat+"&lon="+userLong+"&appid=8ca4ab24addcdc2731bf0de44f51b61d&units=metric";

        // String Request initialized
        mStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject usersObject = new JSONObject(response);
                    Log.i(TAG, response.toString());
                    String temp = usersObject.getJSONObject("main").getString("temp");
                    JSONArray results = usersObject.getJSONArray("weather");
                    JSONObject first = results.getJSONObject(0);
                    String detail = first.getString("main");
                    Log.i(TAG, detail);
                    weatherText2.setText(detail);
                    Log.i(TAG, temp);
                    weatherText.setText((temp) + " \u2103");
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error :" + error.toString());
            }
        });

        mRequestQueue.add(mStringRequest);
    }

    public void onClickRecord(View v) {
        // go to the record journey activity
        Intent journey = new Intent(MainActivity.this, RecordJourney.class);
        startActivity(journey);
    }

    public void onClickView(View v) {
        // go to the activity for displaying journeys
        Intent view = new Intent(MainActivity.this, ViewJourneys.class);
        startActivity(view);
    }

    public void onClickStatistics(View v) {
        // go to the activity for displaying statistics
        Intent stats = new Intent(MainActivity.this, Statistics.class);
        startActivity(stats);
    }
    public static class NoPermissionDialogue extends DialogFragment {

        public static MainActivity.NoPermissionDialogue newInstance() {
            return new MainActivity.NoPermissionDialogue();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

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