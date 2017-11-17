package com.demo.takehometest.model;

import android.location.Location;

/**
 * Created by frodberserk on 11/16/2017.
 */

public class MainActivityModel {

    private boolean tracking = false;
    private Location location;

    public MainActivityModel() {
        tracking = false;
        location = null;
    }

    public void setTracking(boolean flag) {
        tracking = flag;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}