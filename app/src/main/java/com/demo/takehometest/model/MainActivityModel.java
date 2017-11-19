package com.demo.takehometest.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Model class for main activity.
 */
public class MainActivityModel {

    /**
     * Stores current journey.
     */
    private ArrayList<LatLng> locations;

    public MainActivityModel() {
        locations = new ArrayList<>();
    }

    /**
     * @return
     */
    public ArrayList<LatLng> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LatLng> locations) {
        this.locations = locations;
    }

    public void addJourney(Location location) {
        this.locations.add(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    /**
     * Clear the ongoing journey.
     */
    public void clearJourney() {
        locations.clear();
    }
}