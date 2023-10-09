package com.example.main;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.main.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private long journeyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        Bundle bundle;
        bundle = getIntent().getExtras();
        journeyID = bundle.getLong("journeyID");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        Cursor c = getContentResolver().query(JPC.LOCATION_URI,
                null, JPC.L_JID + " = " + journeyID, null, null);

        PolylineOptions line = new PolylineOptions().clickable(false);
        LatLng firstLoc;
        firstLoc = null;
        LatLng lastLoc;
        lastLoc = null;
        try {
            while(c.moveToNext()) {
                @SuppressLint("Range") LatLng loc = new LatLng(c.getDouble(c.getColumnIndex(JPC.L_LATITUDE)),
                        c.getDouble(c.getColumnIndex(JPC.L_LONGITUDE)));
                if(c.isLast()) {
                    lastLoc = loc;
                }
                if(c.isFirst()) {
                    firstLoc = loc;
                }

                line.add(loc);
            }
        } finally {
            c.close();
        }

        float zoom;
        zoom = 15.0f;
        if(lastLoc != null && firstLoc != null) {
            mMap.addMarker(new MarkerOptions().position(firstLoc).title("Start"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, zoom));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.addMarker(new MarkerOptions().position(lastLoc).title("End"));
        }
        mMap.addPolyline(line);
    }
}