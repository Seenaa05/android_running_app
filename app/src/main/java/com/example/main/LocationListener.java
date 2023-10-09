package com.example.main;
import android.location.Location;


import java.util.ArrayList;
public class LocationListener implements android.location.LocationListener {
    ArrayList<Location> locations;
    boolean recordLocations;
    int km;

    public LocationListener() {
        locations = new ArrayList<Location>();
        recordLocations = false;
    }
    public void newJourney() {
        locations = new ArrayList<Location>();
    }

    public float getDistanceOfJourney() {
        return locations.size() <= 1 ? 0 : locations.get(0).distanceTo(locations.get(locations.size() - 1)) / 1000;


    }

    public ArrayList<Location> getLocations() {
        ArrayList<Location> locations = this.locations;
        return locations;
    }


    @Override
    public void onLocationChanged(Location location) {
        // called when the location is changed. Can obtain latitude, longitude, altitude.
        if(recordLocations) {
            locations.add(location);
        }
    }
}