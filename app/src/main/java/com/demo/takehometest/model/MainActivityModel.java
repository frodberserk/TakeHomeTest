package com.demo.takehometest.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Model class for main activity.
 */
public class MainActivityModel {

    /**
     * boolean to store tracking on/off flag.
     */
    private boolean tracking;

    /**
     * Saves current location.
     */
    private Location location;

    private ArrayList<LatLng> locations;

    public MainActivityModel() {
        tracking = false;
        location = null;
        locations = new ArrayList<>();
    }

    /**
     * Checks if tracking is on/off.
     *
     * @return true if tracking is on.
     */
    public boolean isTracking() {
        return tracking;
    }

    /**
     * Sets the status for tracking.
     *
     * @param flag true to set tracking on, false to set tracking off.
     */
    public void setTracking(boolean flag) {
        tracking = flag;
    }

    /**
     * Sets the location parameter.
     *
     * @param location Location object to be saved.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    public ArrayList<LatLng> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LatLng> locations) {
        this.locations = locations;
    }

    public void addJourney(Location location) {
        this.locations.add(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    public void clearJourney() {
        locations.clear();
    }
}