package com.demo.takehometest.model;

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

    public ArrayList<LatLng> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LatLng> locations) {
        this.locations = locations;
    }

    public void addToJourney(LatLng latlng) {
        this.locations.add(latlng);
    }

    /**
     * Clear the ongoing journey.
     */
    public void clearJourney() {
        locations.clear();
    }
}