package com.demo.takehometest.controller;

import android.location.Location;

import com.demo.takehometest.listener.LocationUpdateListener;
import com.demo.takehometest.model.MainActivityModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by frodberserk on 11/16/2017.
 */

public class MainActivityController {

    private MainActivityModel model;
    private LocationUpdateListener mLocationUpdateListener;

    public MainActivityController() {
        model = new MainActivityModel();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(Location location) {
        model.setLocation(location);
        if (mLocationUpdateListener != null) {
            mLocationUpdateListener.onLocationReceived(location);
        }
    }

    public void registerForLocationUpdates(LocationUpdateListener locationUpdateListener) {
        mLocationUpdateListener = locationUpdateListener;
    }
}