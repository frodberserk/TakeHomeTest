package com.demo.takehometest.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.demo.takehometest.R;
import com.demo.takehometest.controller.MainActivityController;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Google Map";

    private Unbinder mUnbinder;
    private GoogleMap mGoogleMap;
    private MapFragment mMapFragment;

    private MainActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);

        controller = new MainActivityController();

        // Add google MapFragment to display map
        mMapFragment = new MapFragment();
        getFragmentManager().beginTransaction().add(R.id.container, mMapFragment, "mapFragment").commit();
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        mGoogleMap = googleMap;
    }
}