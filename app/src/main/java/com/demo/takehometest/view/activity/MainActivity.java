package com.demo.takehometest.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.demo.takehometest.R;
import com.demo.takehometest.controller.MainActivityController;
import com.demo.takehometest.listener.MapUpdateViewCallback;
import com.demo.takehometest.util.AppConstants;
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
        implements OnMapReadyCallback, MapUpdateViewCallback, CompoundButton.OnCheckedChangeListener {

    /**
     * Simple tag for map logs.
     */
    private static final String TAG = "Google Map";

    /**
     * Width of path drawn on map.
     */
    private static final int POLYLINE_WIDTH = 5;

    /**
     * Color of path drawn on map.
     */
    private static final int POLYLINE_COLOR = Color.BLUE;

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
    @BindView(R.id.parent)
    View parentView;

    /**
     * Butter knife view binder.
     */
    private Unbinder mUnbinder;

    /**
     * Google map elements.
     */
    private GoogleMap mGoogleMap;

    /**
     * Controller for the current view.
     */
    private MainActivityController controller;

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
        MapFragment mMapFragment = new MapFragment();
        getFragmentManager().beginTransaction().add(R.id.container, mMapFragment, "mapFragment").commit();
        mMapFragment.getMapAsync(this);

        swTracking.setOnCheckedChangeListener(this);
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

    /**
     * Stops the tracking.
     */
    private void stopTracking() {
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
        controller.bindToLocationService();
    }

    @Override
    protected void onStop() {
        //Activity is no longer visible.
        super.onStop();
        //Unregister map UI updates
        controller.unregisterForUpdateUI();
        //If service bound, unbind.
        controller.unbindToLocationService();
    }

    @Override
    public void onDestroy() {
        //Called when activity is destroyed. Unbind the views.
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        //Callback for when the permission is requested.

        //Check for the resultCode to see what permission was requested.
        switch (requestCode) {
            case AppConstants.REQUEST_CODE_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted. Update tracking UI, and start updates.
                    swTracking.setChecked(true);
                    controller.requestLocationUpdates();
                } else {
                    //If tracking was enabled but permission wasn't granted, turn off switch.
                    //Disabled listener before toggling it off so that the manual toggle won't
                    //trigger listener.
                    swTracking.setOnCheckedChangeListener(null);
                    swTracking.setChecked(false);
                    swTracking.setOnCheckedChangeListener(this);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Called when map is ready to display.
        Log.d(TAG, "OnMapReady");
        mGoogleMap = googleMap;
    }

    /**
     * Called when need to update view.
     *
     * @param data List of journey points.
     */
    @Override
    public void updateView(ArrayList<LatLng> data) {
        //Display user's current location on map.
        drawJourney(data);
    }

    @Override
    public void displayAlert(String message) {
        Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Draw's the journey so far.
     */
    private void drawJourney(ArrayList<LatLng> journey) {
        //Clear map
        mGoogleMap.clear();

        //Return if no data.
        if (journey.size() == 0) return;

        //Describe PolylineOptions, specifying points to connect.
        PolylineOptions options = new PolylineOptions().width(POLYLINE_WIDTH).color(POLYLINE_COLOR)
                .geodesic(true);

        //Add all points to PolylineOptions
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

        //Animate camera to current location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentMarker).zoom(zoom).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        //Draw the initial point marker if possible
        if (journey.size() > 1) {
            //Journey has more than one pointers. So, we can add initial marker on first location.
            LatLng initialLocation = new LatLng(journey.get(0).latitude, journey.get(0).longitude);

            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .position(initialLocation)
                    .title(getString(R.string.starting_point)));
        }
    }

    /**
     * Open @{@link JourneyListActivity}.
     */
    private void openJourneyListActivity() {
        startActivity(new Intent(this, JourneyListActivity.class));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            //Tracking switch was turned on.
            controller.requestForTracking();
        } else {
//            Tracking switch was turned off
            stopTracking();
        }
    }
}