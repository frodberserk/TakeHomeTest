package com.demo.takehometest.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.demo.takehometest.R;
import com.demo.takehometest.controller.DisplayJourneyController;
import com.demo.takehometest.listener.JourneyPathQueryCallback;
import com.demo.takehometest.model.LocationPoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
     * Initial zoom to map.
     */
    private static final float INITIAL_ZOOM = 17.0f;

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
    public void onQuerySuccessful(List<LocationPoint> points) {
        //Clear map
        mGoogleMap.clear();

        //No journey points found, unlikely to happen
        if (points.size() == 0) {
            return;
        }

        //Camera focus point
        LatLng cameraFocusPoint;

        //Single point journey
        if (points.size() == 1) {
            //Just draw single marker. Camera focus point will be the marker itself.
            cameraFocusPoint = new LatLng(points.get(0).getLatitude(),
                    points.get(0).getLongitude());
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(cameraFocusPoint)
                    .title(getString(R.string.single_point_journey)));
        } else {
            //Display journey path here
            PolylineOptions options = new PolylineOptions().width(POLYLINE_WIDTH)
                    .color(POLYLINE_COLOR)
                    .geodesic(true);
            for (int i = 0; i < points.size(); i++) {
                LatLng point = new LatLng(points.get(i).getLatitude(),
                        points.get(i).getLongitude());
                options.add(point);
            }
            mGoogleMap.addPolyline(options);

            //Define start and end points
            LatLng startPoint = new LatLng(points.get(0).getLatitude(),
                    points.get(0).getLongitude());
            LatLng endPoint = new LatLng(points.get(points.size() - 1).getLatitude(),
                    points.get(points.size() - 1).getLongitude());

            //Define camera focus point as mid point between start and end
            cameraFocusPoint = new LatLngBounds(startPoint, endPoint).getCenter();

            //Add markers for start and end
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .position(startPoint)
                    .title(getString(R.string.starting_point)));
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(endPoint)
                    .title(getString(R.string.ending_point)));
        }

        //Animate camera to center point of journey
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(cameraFocusPoint).zoom(INITIAL_ZOOM).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }
}