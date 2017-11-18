package com.demo.takehometest.controller;

import android.content.Context;
import android.location.Location;

import com.demo.takehometest.listener.LocationUpdateListener;
import com.demo.takehometest.model.MainActivityModel;
import com.demo.takehometest.util.PreferencesUtil;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

/**
 * Controller class for @{@link com.demo.takehometest.view.activity.MainActivity}.
 */
public class MainActivityController {

    /**
     * Defines model object.
     */
    private MainActivityModel model;

    /**
     * Listener used to get location updates from background service.
     */
    private LocationUpdateListener mLocationUpdateListener;

    /**
     * Util class to save/retrieve data saved in preferences.
     */
    private PreferencesUtil mPreferencesUtil;

    public MainActivityController(Context context) {
        model = new MainActivityModel();
        mPreferencesUtil = new PreferencesUtil(context);

        model.setTracking(mPreferencesUtil.isTrackingOn());
    }

    /**
     * Called when received a location event.
     *
     * @param location Location data fetched from background service.
     */
    @Subscribe
    public void onEvent(Location location) {
        model.setLocation(location);
        addToJourney(location);
        if (mLocationUpdateListener != null) {
            mLocationUpdateListener.onLocationReceived(location);
        }
    }

    private void addToJourney(Location location) {
        model.addJourney(location);
    }

    /**
     * Register for location updates from background service.
     *
     * @param locationUpdateListener The event listener passed to listen to location events.
     *                               If null is passed, the controller will unregister to location
     *                               events.
     */
    public void registerForLocationUpdates(LocationUpdateListener locationUpdateListener) {
        if (locationUpdateListener != null) {
            //If View register controller for receiving location updates, we enable subscriber for
            //event bus to receive location events.
            mLocationUpdateListener = locationUpdateListener;
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        } else {
            //Updates disabled. Unregister event bus.
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        }
    }

    /**
     * Saves tracking status to the model object.
     *
     * @param status true if enabled, false if disabled.
     */
    public void setTrackingStatus(boolean status) {
        model.setTracking(status);
        mPreferencesUtil.setTracking(status);

        //If false, clear current journey.
        if (!status) {
            model.clearJourney();
        }
    }

    /**
     * Return if tracking field is on in Model class.
     *
     * @return true or false
     */
    public boolean isTrackingOn() {
        return model.isTracking();
    }

    /**
     * Return list of coordinates user travelled.
     *
     * @return ArrayList of location.
     */
    public ArrayList<LatLng> getJourney() {
        return model.getLocations();
    }
}