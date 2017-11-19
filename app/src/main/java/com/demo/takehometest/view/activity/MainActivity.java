package com.demo.takehometest.view.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.demo.takehometest.R;
import com.demo.takehometest.controller.MainActivityController;
import com.demo.takehometest.listener.UpdateViewCallback;
import com.demo.takehometest.service.LocationUpdatesService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Main activity of the application.
 * <p>
 * This activity is used to display map and user's location on it.
 */
public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, UpdateViewCallback {

    /**
     * Simple tag for map logs.
     */
    private static final String TAG = "Google Map";

    /**
     * Permission request codes.
     */
    private static final int PERMISSION_REQUEST_LOCATION = 1;

    /**
     * Initial zoom to map.
     */
    private static final float INITIAL_ZOOM = 17.0f;

    /**
     * Used to check if it is first zoom to map, so that we can set initial zoom state to map.
     */
    private boolean initialZoomMap = true;

    /**
     * Views to bind.
     */
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

            //If tracking is enabled, check for location permission.
            if (controller.isTrackingOn()) {
                drawJourney(controller.getJourney());

                if (mLocationUpdatesService.isUpdateOn()) {
                    return;
                }
                //Check if location permission is granted.
                if (isLocationPermissionGranted()) {
                    //Permission is granted, start location service.
                    startLocationUpdates();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind the views.
        mUnbinder = ButterKnife.bind(this);

        //Initialize the controller.
        controller = new MainActivityController(this);

        //Update views
        swTracking.setChecked(controller.isTrackingOn());

        // Add google MapFragment to display map
        mMapFragment = new MapFragment();
        getFragmentManager().beginTransaction().add(R.id.container, mMapFragment, "mapFragment").commit();
        mMapFragment.getMapAsync(this);

        addSwitchListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_show_journeys:
                openJourneyListActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addSwitchListener() {
        swTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                controller.setTrackingStatus(b);
                if (b) {
                    if (isLocationPermissionGranted() && controller.isTrackingOn()) {
                        startLocationUpdates();
                    } else {
                        requestForLocationPermission();
                    }
                } else {
                    stopTracking();
                }
            }
        });
    }

    /**
     * Stops the tracking by turning off updates from service.
     */
    private void stopTracking() {
        mLocationUpdatesService.removeLocationUpdates();

        controller.setTrackingStatus(false);

        //Clear the map.
        mGoogleMap.clear();
    }

    @Override
    protected void onStart() {
        //Check if permission for location is granted.
        //This also helps in checking if the user has revoked location permission from settings.
        super.onStart();

        //Register controller to check for map UI updates.
        controller.registerForUpdateUI(this);

        //Bind service
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        //Activity is no longer visible.
        super.onStop();
        //Unregister map UI updates
        controller.unregisterForUpdateUI();
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
                    // Permission granted. Update tracking UI, and start updates.
                    swTracking.setChecked(true);
                    controller.setTrackingStatus(true);
                    startLocationUpdates();
                } else {
                    //If tracking was enabled but permission wasn't granted, turn off tracking.
                    swTracking.setChecked(false);
                    controller.setTrackingStatus(false);
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
     * Called when received a location update. Update the journey here.
     */
    @Override
    public void updateView() {
        //Display user's current location on map.
        drawJourney(controller.getJourney());
    }

    /**
     * Draw's the journey so far.
     */
    private void drawJourney(ArrayList<LatLng> journey) {
        //Clear map
        mGoogleMap.clear();

        //Return if no data.
        if (journey.size() == 0) return;

        //Describe polyline options, specifying points to connect.
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < journey.size(); i++) {
            LatLng point = journey.get(i);
            options.add(point);
        }

        //Draw the path.
        mGoogleMap.addPolyline(options);


        //Update markers
        LatLng currentMarker = journey.get(journey.size() - 1);
        mGoogleMap.addMarker(new MarkerOptions()
                .position(currentMarker)
                .title(getString(R.string.my_location)));

        //Animate the camera to new location.
        float zoom;
        if (initialZoomMap) {
            //First zoom, so set default zoom.
            zoom = INITIAL_ZOOM;
            initialZoomMap = false;
        } else {
            //Not first zoom, so use the current zoom of map to avoid UI flickering of map.
            zoom = mGoogleMap.getCameraPosition().zoom;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentMarker).zoom(zoom).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        //Draw the initial point marker if possible
        if (journey.size() > 1) {
            //Journey has more than one pointers. So, we can add initial marker on first location.
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(currentMarker)
                    .title(getString(R.string.my_location)));
        }
    }

    /**
     * Open @{@link JourneyListActivity}.
     */
    private void openJourneyListActivity() {
        startActivity(new Intent(this, JourneyListActivity.class));
    }
}