package com.demo.takehometest.controller;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.demo.takehometest.listener.RequestJourneyCallback;
import com.demo.takehometest.listener.MapUpdateViewCallback;
import com.demo.takehometest.model.LocationPoint;
import com.demo.takehometest.model.MainActivityModel;
import com.demo.takehometest.service.LocationUpdatesService;
import com.demo.takehometest.util.AppConstants;
import com.demo.takehometest.util.PreferencesUtil;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for @{@link com.demo.takehometest.view.activity.MainActivity}.
 */
public class MainActivityController implements RequestJourneyCallback {

    /**
     * Saves the context of activity.
     */
    private Context context;

    /**
     * Defines model object.
     */
    private MainActivityModel model;

    /**
     * Listener used to get location updates from background service.
     */
    private MapUpdateViewCallback mUpdateViewCallback;

    /**
     * Util class to save/retrieve data saved in preferences.
     */
    private PreferencesUtil mPreferencesUtil;

    /**
     * Object for location service class.
     */
    private LocationUpdatesService mLocationUpdatesService;

    /**
     * Monitors the state of the connection to the service.
     */
    private boolean bound = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mLocationUpdatesService = binder.getService();
            bound = true;

            //If tracking is enabled, check for location permission.
            if (isTrackingOn()) {
                if (mLocationUpdatesService.isUpdateOn()) {
                    //Service is running location updates, so update current journey data.
                    syncCurrentJourneyData();
                    return;
                }

                //Check if location permission is granted.
                if (isLocationPermissionGranted()) {
                    //Permission is granted, start location service.
                    requestLocationUpdates();
                } else {
                    //Permission is not granted so requesting for it.
                    requestForLocationPermission();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationUpdatesService = null;
            bound = false;
        }
    };

    public MainActivityController(Context context) {
        this.context = context;
        model = new MainActivityModel();
        mPreferencesUtil = new PreferencesUtil(context);
    }

    /**
     * Called when received a location event.
     *
     * @param location Location data fetched from background service.
     */
    @Subscribe
    public void onEvent(Location location) {
        addToJourney(location);
        updateView();
    }

    private void addToJourney(Location location) {
        model.addToJourney(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    /**
     * Register for UI updates of map.
     *
     * @param updateViewCallback The event listener passed to listen to view updates for map.
     */
    public void registerForUpdateUI(MapUpdateViewCallback updateViewCallback) {
        //If View register controller for receiving UI updates of journey, we enable subscriber
        // for event bus to receive location events.
        mUpdateViewCallback = updateViewCallback;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * Unregister for UI updates of map.
     */
    public void unregisterForUpdateUI() {
        //Updates disabled. Unregister event bus.
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * Saves tracking status to the model object.
     *
     * @param status true if enabled, false if disabled.
     */
    public void setTrackingStatus(boolean status) {
        mPreferencesUtil.setTracking(status);

        //If false, clear current journey.
        if (!status) {
            mLocationUpdatesService.removeLocationUpdates();
            model.clearJourney();
        }
    }

    /**
     * Return if tracking field is on in Model class.
     *
     * @return true or false
     */
    public boolean isTrackingOn() {
        return mPreferencesUtil.isTrackingOn();
    }

    /**
     * Return list of coordinates user travelled.
     *
     * @return ArrayList of location.
     */
    public ArrayList<LatLng> getJourney() {
        return model.getLocations();
    }

    /**
     * Bind to background location service.
     */
    public void bindToLocationService() {
        context.bindService(new Intent(context, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind to background location service.
     */
    public void unbindToLocationService() {
        if (bound) {
            context.unbindService(mServiceConnection);
            bound = false;
        }
    }

    public void requestLocationUpdates() {
        setTrackingStatus(true);
        mLocationUpdatesService.requestLocationUpdates();
    }

    /**
     * Check if location permission is granted.
     *
     * @return True if granted, false otherwise.
     */
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request controller for tracking user location. If location permission is not granted, the
     * controller will request for permission.
     * <p>
     * If user doesn't allow permission to track location, tracking won't start.
     */
    public void requestForTracking() {
        if (isLocationPermissionGranted()) {
            //Start location updates
            requestLocationUpdates();
        } else {
            //Request for location permission
            requestForLocationPermission();
        }
    }

    /**
     * This method is called to request for location permission.
     */
    private void requestForLocationPermission() {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                AppConstants.REQUEST_CODE_LOCATION_PERMISSION);
    }

    private void syncCurrentJourneyData() {
        mLocationUpdatesService.requestCurrentJourneyData(this);
    }

    @Override
    public void onRequestComplete(List<LocationPoint> data) {
        //Insert location data into model
        model.clearJourney();
        for (int i = 0; i < data.size(); i++) {
            LatLng latLng = new LatLng(data.get(i).getLatitude(), data.get(i).getLongitude());
            model.addToJourney(latLng);
        }
        updateView();
    }

    /**
     * Update the view.
     */
    private void updateView() {
        if (mUpdateViewCallback != null) {
            mUpdateViewCallback.updateView(getJourney());
        }
    }
}