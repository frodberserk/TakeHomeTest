package com.demo.takehometest.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.demo.takehometest.R;
import com.demo.takehometest.controller.DisplayJourneyController;
import com.demo.takehometest.listener.JourneyPathQueryCallback;
import com.demo.takehometest.util.AppConstants;
import com.demo.takehometest.util.AppMethods;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Activity to display the journey on the map.
 */

public class DisplayJourneyActivity extends AppCompatActivity
        implements OnMapReadyCallback, JourneyPathQueryCallback {

    /**
     * Simple tag for map logs.
     */
    private static final String TAG = "Google Map";

    /**
     * Key in which activity will receive journey id.
     */
    public static final String EXTRA_JOURNEY_ID = "extra_journey_id";

    /**
     * Width of path drawn on map.
     */
    private static final int POLYLINE_WIDTH = 5;

    /**
     * Color of path drawn on map.
     */
    private static final int POLYLINE_COLOR = Color.BLUE;


    /**
     * Google map elements.
     */
    private GoogleMap mGoogleMap;

    /**
     * Controller for the current view.
     */
    private DisplayJourneyController controller;

    private long journeyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_journey);

        //Retrieve journey id from intent
        journeyId = getIntent().getLongExtra(EXTRA_JOURNEY_ID, 0);

        setTitle(getString(R.string.journey_display_title, journeyId));

        //Initialize the controller.
        controller = new DisplayJourneyController(this);

        // Add google MapFragment to display map
        MapFragment mMapFragment = new MapFragment();
        getFragmentManager().beginTransaction().add(R.id.container, mMapFragment, "mapFragment")
                .commit();
        mMapFragment.getMapAsync(this);
    }

    /**
     * Called when map is ready to display.
     *
     * @param googleMap GoogleMap object.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        mGoogleMap = googleMap;
        controller.fetchJourneyPath(journeyId, this);
    }

    /**
     * Callback when controller sends journey data.
     *
     * @param points List containing journey points.
     */
    @Override
    public void onQuerySuccessful(List<LatLng> points) {
        //Clear map
        mGoogleMap.clear();

        //No journey points found, unlikely to happen
        if (points.size() == 0) {
            return;
        }

        //Single point journey
        if (points.size() == 1) {
            //Just draw single marker
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(points.get(0))
                    .title(getString(R.string.single_point_journey)));
        } else {
            //Display journey path here
            PolylineOptions options = new PolylineOptions().width(POLYLINE_WIDTH)
                    .color(POLYLINE_COLOR)
                    .geodesic(true);
            options.addAll(points);
            mGoogleMap.addPolyline(options);

            //Add markers for start and end
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .position(points.get(0))
                    .title(getString(R.string.starting_point)));
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(points.get(points.size() - 1))
                    .title(getString(R.string.ending_point)));
        }

        //Animate camera to journey
        AppMethods.zoomRoute(mGoogleMap, points, AppConstants.MAP_PADDING);
    }
}