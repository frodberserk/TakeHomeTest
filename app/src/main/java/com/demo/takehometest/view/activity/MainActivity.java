package com.demo.takehometest.view.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Switch;

import com.demo.takehometest.R;
import com.demo.takehometest.controller.MainActivityController;
import com.demo.takehometest.listener.LocationUpdateListener;
import com.demo.takehometest.service.LocationUpdatesService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Main activity of the application.
 * <p>
 * This activity is used to display map and user's location on it.
 */
public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, LocationUpdateListener {

    /**
     * Simple tag for map logs.
     */
    private static final String TAG = "Google Map";

    /**
     * Permission request codes.
     */
    private static final int PERMISSION_REQUEST_LOCATION = 1;

    @BindView(R.id.sw_tracking)
    Switch swTracking;

    /**
     * Butter knife view binder.
     */
    private Unbinder mUnbinder;

    /**
     * Google map elements.
     */
    private GoogleMap mGoogleMap;
    private MapFragment mMapFragment;

    /**
     * Controller for the current view.
     */
    private MainActivityController controller;
    // Monitors the state of the connection to the service.

    /**
     * Object for location service class.
     */
    private LocationUpdatesService mLocationUpdatesService;
    private boolean bound = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mLocationUpdatesService = binder.getService();
            bound = true;
            //Check if location permission is granted.
            if (isLocationPermissionGranted()) {
                //Permission is granted, start location service.
                startLocationUpdates();
            } else {
                //Permission is not granted so requesting for it.
                requestForLocationPermission();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationUpdatesService = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind the views.
        mUnbinder = ButterKnife.bind(this);

        //Initialize the controller.
        controller = new MainActivityController();

        // Add google MapFragment to display map
        mMapFragment = new MapFragment();
        getFragmentManager().beginTransaction().add(R.id.container, mMapFragment, "mapFragment").commit();
        mMapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        //Check if permission for location is granted.
        //This also helps in checking if the user has revoked location permission from settings.
        super.onStart();

        //Register controller to check for location updates.
        controller.registerForLocationUpdates(this);

        //Bind service
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        //Activity is no longer visible.
        super.onStop();
        //Unregister location updates
        controller.registerForLocationUpdates(null);
        //If service bound, unbind.
        if (bound) {
            unbindService(mServiceConnection);
            bound = false;
        }
    }

    @Override
    public void onDestroy() {
        //Called when activity is destroyed. Unbind the views.
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * Check if location permission is granted.
     *
     * @return True if granted, false otherwise.
     */
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * This method is called to request for location permission.
     */
    private void requestForLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        //Callback for when the permission is requested.

        //Check for the resultCode to see what permission was requested.
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted.
                        mLocationUpdatesService.requestLocationUpdates();
                    }
                }
            }
        }
    }

    /**
     * Start location updates from @{@link LocationUpdatesService}.
     */
    private void startLocationUpdates() {
        mLocationUpdatesService.requestLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Called when map is ready to display.
        Log.d(TAG, "OnMapReady");
        mGoogleMap = googleMap;
    }

    /**
     * @param location The location object received
     */
    @Override
    public void onLocationReceived(Location location) {
        //Display user's current location on map.
        moveToLocation(location);
    }


    /**
     * Move to the location in the map.
     *
     * @param location The location to which we need to move.
     */
    private void moveToLocation(Location location) {
        //Clear old marker.
        mGoogleMap.clear();

        //Add new marker on current location.
        LatLng defaultLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mGoogleMap.addMarker(new MarkerOptions()
                .position(defaultLocation)
                .title(getString(R.string.my_location)));

        //Animate the camera to new location.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(defaultLocation).zoom(18f).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }
}